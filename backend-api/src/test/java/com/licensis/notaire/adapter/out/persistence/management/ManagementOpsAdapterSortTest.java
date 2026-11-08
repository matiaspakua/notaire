package com.licensis.notaire.adapter.out.persistence.management;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

/**
 * Newest-first sort must survive the ports-only list path (#1772 / #1340 / #1347).
 */
class ManagementOpsAdapterSortTest {

    @Test
    @DisplayName("blank sort defaults to idManagement desc")
    void blankDefaultsToNewestFirst() {
        Sort sort = ManagementOpsAdapter.parseSort(null);
        assertThat(sort.getOrderFor("idManagement").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("idManagement,desc keeps descending order")
    void explicitDesc() {
        Sort sort = ManagementOpsAdapter.parseSort("idManagement,desc");
        assertThat(sort.getOrderFor("idManagement").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("idManagement,asc keeps ascending order")
    void explicitAsc() {
        Sort sort = ManagementOpsAdapter.parseSort("idManagement,asc");
        assertThat(sort.getOrderFor("idManagement").getDirection()).isEqualTo(Sort.Direction.ASC);
    }
}
