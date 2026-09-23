package com.gymmanagement.attendance;

import com.gymmanagement.attendance.dto.*;
import com.gymmanagement.client.*;
import com.gymmanagement.common.dto.PageResponse;
import com.gymmanagement.common.exception.*;
import com.gymmanagement.membership.*;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final ClientRepository clientRepository;
    private final MembershipRepository membershipRepository;

    @Transactional
    public AttendanceResponse checkIn(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", clientId));
        validateMembership(client);
        if (attendanceRepository.findByClientIdAndCheckOutAtIsNull(clientId).isPresent()) {
            throw new BadRequestException("Client already has an active check-in");
        }
        try {
            Attendance attendance = attendanceRepository.saveAndFlush(Attendance.builder()
                    .client(client).branch(client.getUser().getBranch()).checkInAt(LocalDateTime.now())
                    .activeKey("CLIENT-" + clientId).build());
            return toResponse(attendance);
        } catch (DataIntegrityViolationException exception) {
            throw new BadRequestException("Client already has an active check-in");
        }
    }

    @Transactional
    public AttendanceResponse checkOut(Long clientId) {
        Attendance attendance = attendanceRepository.findByClientIdAndCheckOutAtIsNull(clientId)
                .orElseThrow(() -> new BadRequestException("Client has no active check-in"));
        LocalDateTime checkOut = LocalDateTime.now();
        attendance.setCheckOutAt(checkOut);
        attendance.setDurationMinutes(Duration.between(attendance.getCheckInAt(), checkOut).toMinutes());
        attendance.setActiveKey(null);
        return toResponse(attendanceRepository.save(attendance));
    }

    public PageResponse<AttendanceResponse> history(Long clientId, int page, int size) {
        return PageResponse.from(attendanceRepository.findByClientId(clientId,
                PageRequest.of(page, size, Sort.by("checkInAt").descending())).map(this::toResponse));
    }

    public List<AttendanceResponse> report(LocalDate from, LocalDate to) {
        validateRange(from, to);
        return attendanceRepository.findByCheckInAtBetweenOrderByCheckInAtDesc(from.atStartOfDay(), to.plusDays(1).atStartOfDay())
                .stream().map(this::toResponse).toList();
    }

    public UsageReportResponse usage(Long clientId, LocalDate from, LocalDate to) {
        validateRange(from, to);
        Object[] aggregate = attendanceRepository.usage(clientId, from.atStartOfDay(), to.plusDays(1).atStartOfDay());
        Object[] row = aggregate.length == 1 && aggregate[0] instanceof Object[] nested ? nested : aggregate;
        long visits = ((Number) row[0]).longValue();
        long minutes = ((Number) row[1]).longValue();
        return new UsageReportResponse(clientId, visits, minutes, visits == 0 ? 0 : (double) minutes / visits);
    }

    public List<PeakHourResponse> peakHours(LocalDate from, LocalDate to) {
        validateRange(from, to);
        return attendanceRepository.peakHours(from.atStartOfDay(), to.plusDays(1).atStartOfDay(), PageRequest.of(0, 24))
                .stream().map(row -> new PeakHourResponse(((Number) row[0]).intValue(), ((Number) row[1]).longValue())).toList();
    }

    private void validateMembership(Client client) {
        Membership membership = membershipRepository.findFirstByClientIdOrderByEndDateDesc(client.getId())
                .orElseThrow(() -> new BadRequestException("Client has no membership"));
        LocalDate today = LocalDate.now();
        if (!client.getUser().isActive() || membership.getStatus() == MembershipStatus.SUSPENDED) {
            throw new BadRequestException("Suspended membership cannot check in");
        }
        if (membership.getStatus() != MembershipStatus.ACTIVE || today.isBefore(membership.getStartDate())
                || today.isAfter(membership.getEndDate())) {
            throw new BadRequestException("Expired or inactive membership cannot check in");
        }
    }

    private void validateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to) || from.plusYears(1).isBefore(to)) {
            throw new BadRequestException("Report range must be valid and no longer than one year");
        }
    }

    private AttendanceResponse toResponse(Attendance attendance) {
        return new AttendanceResponse(attendance.getId(), attendance.getClient().getId(),
                attendance.getClient().getUser().getFullName(), attendance.getCheckInAt(),
                attendance.getCheckOutAt(), attendance.getDurationMinutes());
    }
}