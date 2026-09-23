package com.gymmanagement.attendance;

import com.gymmanagement.client.Client;
import com.gymmanagement.client.ClientRepository;
import com.gymmanagement.common.config.JpaAuditingConfig;
import com.gymmanagement.user.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@Import(JpaAuditingConfig.class)
class AttendanceRepositoryIntegrationTest {

    @Autowired AttendanceRepository attendanceRepository;
    @Autowired ClientRepository clientRepository;
    @Autowired UserRepository userRepository;

    @Test
    void databaseRejectsTwoActiveCheckInsForSameClient() {
        User user = userRepository.save(User.builder().firstName("Asha").lastName("Patel")
                .email("attendance@example.com").password("x").role(Role.CLIENT).active(true).build());
        Client client = clientRepository.save(Client.builder().user(user).build());
        attendanceRepository.saveAndFlush(Attendance.builder().client(client).checkInAt(LocalDateTime.now())
                .activeKey("CLIENT-" + client.getId()).build());

        assertThrows(DataIntegrityViolationException.class, () -> attendanceRepository.saveAndFlush(
                Attendance.builder().client(client).checkInAt(LocalDateTime.now()).activeKey("CLIENT-" + client.getId()).build()));
    }
}