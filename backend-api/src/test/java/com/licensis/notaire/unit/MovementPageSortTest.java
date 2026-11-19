package com.licensis.notaire.unit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import com.licensis.notaire.adapter.out.persistence.testimony.MovementPageSort;

class MovementPageSortTest {

    @Test
    void unknownPropertyFallsBackToNewestId() {
        Sort sort = MovementPageSort.of("notAField,asc");
        assertThat(sort.getOrderFor("idTestimonyMovement").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void dateEntryAscendingIsKept() {
        Sort sort = MovementPageSort.of("dateEntry,asc");
        assertThat(sort.getOrderFor("dateEntry").getDirection()).isEqualTo(Sort.Direction.ASC);
    }
}
