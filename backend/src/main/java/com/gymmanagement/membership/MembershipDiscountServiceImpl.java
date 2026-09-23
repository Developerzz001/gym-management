package com.gymmanagement.membership;

import com.gymmanagement.common.exception.BadRequestException;
import com.gymmanagement.common.exception.DuplicateResourceException;
import com.gymmanagement.common.exception.ResourceNotFoundException;
import com.gymmanagement.membership.dto.MembershipDiscountRequest;
import com.gymmanagement.membership.dto.MembershipDiscountResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MembershipDiscountServiceImpl implements MembershipDiscountService {

    private final MembershipDiscountRepository repository;

    @Override
    @Transactional
    public MembershipDiscountResponse create(MembershipDiscountRequest request) {
        if (repository.existsByNameIgnoreCase(request.name().trim())) {
            throw new DuplicateResourceException("Membership discount name already exists: " + request.name());
        }
        MembershipDiscount discount = MembershipDiscount.builder()
                .name(request.name().trim())
                .percentage(request.percentage())
            .extraFreeDays(request.extraFreeDays())
                .description(trim(request.description()))
                .active(request.active())
                .build();
        return toResponse(repository.save(discount));
    }

    @Override
    @Transactional
    public MembershipDiscountResponse update(Long id, MembershipDiscountRequest request) {
        MembershipDiscount discount = find(id);
        if (repository.existsByNameIgnoreCaseAndIdNot(request.name().trim(), id)) {
            throw new DuplicateResourceException("Membership discount name already exists: " + request.name());
        }
        discount.setName(request.name().trim());
        discount.setPercentage(request.percentage());
        discount.setExtraFreeDays(request.extraFreeDays());
        discount.setDescription(trim(request.description()));
        discount.setActive(request.active());
        return toResponse(repository.save(discount));
    }

    @Override
    @Transactional
    public MembershipDiscountResponse setActive(Long id, boolean active) {
        MembershipDiscount discount = find(id);
        discount.setActive(active);
        return toResponse(repository.save(discount));
    }

    @Override
    public List<MembershipDiscountResponse> list(boolean activeOnly) {
        List<MembershipDiscount> discounts = activeOnly
                ? repository.findByActiveTrueOrderByIdDesc()
                : repository.findAllByOrderByIdDesc();
        return discounts.stream().map(this::toResponse).toList();
    }

    @Override
    public MembershipDiscount getActiveEntity(Long id) {
        MembershipDiscount discount = find(id);
        if (!discount.isActive()) {
            throw new BadRequestException("Selected membership discount is inactive");
        }
        return discount;
    }

    private MembershipDiscount find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membership Discount", "id", id));
    }

    private MembershipDiscountResponse toResponse(MembershipDiscount discount) {
        return new MembershipDiscountResponse(discount.getId(), discount.getName(), discount.getPercentage(),
                discount.getExtraFreeDays(), discount.getDescription(), discount.isActive());
    }

    private String trim(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}