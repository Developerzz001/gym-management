package com.gymmanagement.dashboard;

import com.gymmanagement.common.config.JpaAuditingConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@Import({JpaAuditingConfig.class, DashboardReadRepository.class})
class DashboardReadRepositoryIntegrationTest {

    @Autowired
    private DashboardReadRepository repository;

    @Test
    void allOperationalCategoriesSupportCountPagingAndSearch() {
        LocalDate today = LocalDate.now();
        for (DashboardCategory category : DashboardCategory.values()) {
            assertThat(repository.count(category, null, null, today, today.minusDays(30))).isZero();
            assertThat(repository.find(category, "member", null, null, today, today.minusDays(30),
                    PageRequest.of(0, 20)).getContent()).isEmpty();
            assertThat(repository.count(category, 12L, 4L, today, today.minusDays(30))).isZero();
            assertThat(repository.find(category, "member", 12L, 4L, today, today.minusDays(30),
                PageRequest.of(0, 20)).getContent()).isEmpty();
        }
    }

    @Test
    void dailyCollectionQuerySupportsScopedAndUnscopedRequests() {
        assertThat(repository.collectionRows(null, null, LocalDate.now())).isEmpty();
        assertThat(repository.collectionRows(1L, 1L, LocalDate.now())).isEmpty();
    }
}