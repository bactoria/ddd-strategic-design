package kitchenpos.orders.domain.usecase;

import static kitchenpos.Fixtures.emptyTable1;
import static kitchenpos.Fixtures.emptyTable2;
import static kitchenpos.Fixtures.groupedTable1;
import static kitchenpos.Fixtures.groupedTable2;
import static kitchenpos.Fixtures.orderForTable1;
import static kitchenpos.Fixtures.table1AndTable2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertAll;

import kitchenpos.orders.domain.usecase.TableGroupBo;
import kitchenpos.orders.data.InMemoryOrderDao;
import kitchenpos.orders.domain.entity.OrderDao;
import kitchenpos.orders.data.InMemoryOrderTableDao;
import kitchenpos.orders.domain.entity.OrderTableDao;
import kitchenpos.orders.data.InMemoryTableGroupDao;
import kitchenpos.orders.domain.entity.TableGroupDao;
import kitchenpos.orders.domain.entity.TableGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TableGroupBoTest {

    private final OrderDao orderDao = new InMemoryOrderDao();
    private final OrderTableDao orderTableDao = new InMemoryOrderTableDao();
    private final TableGroupDao tableGroupDao = new InMemoryTableGroupDao();

    private TableGroupBo tableGroupBo;

    @BeforeEach
    void setUp() {
        tableGroupBo = new TableGroupBo(orderDao, orderTableDao, tableGroupDao);
        orderTableDao.save(emptyTable1());
        orderTableDao.save(emptyTable2());
    }

    @DisplayName("2 개 이상의 빈 테이블을 단체로 지정할 수 있다.")
    @Test
    void create() {
        // given
        final TableGroup expected = table1AndTable2();

        // when
        final TableGroup actual = tableGroupBo.create(expected);

        // then
        assertThat(actual).isNotNull();
        assertAll(
            () -> assertThat(actual.getOrderTables())
                .containsExactlyInAnyOrderElementsOf(expected.getOrderTables())
        );
    }

    @DisplayName("단체 지정은 중복될 수 없다.")
    @Test
    void createWithGroupedTable() {
        // given
        orderTableDao.save(groupedTable1());

        final TableGroup expected = table1AndTable2();

        // when
        // then
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> tableGroupBo.create(expected));
    }

    @DisplayName("단체 지정을 해지할 수 있다.")
    @Test
    void ungroup() {
        // given
        final Long tableGroupId = saveTable1AndTable2().getId();

        // when
        // then
        tableGroupBo.ungroup(tableGroupId);
    }

    @DisplayName("단체 지정된 테이블의 주문 상태가 조리 또는 식사인 경우 단체 지정을 해지할 수 없다.")
    @Test
    void ungroupNotCalculatedTableGroup() {
        // given
        final Long tableGroupId = saveTable1AndTable2().getId();
        orderDao.save(orderForTable1());

        // when
        // then
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> tableGroupBo.ungroup(tableGroupId));
    }

    private TableGroup saveTable1AndTable2() {
        final TableGroup tableGroup = tableGroupDao.save(table1AndTable2());
        orderTableDao.save(groupedTable1());
        orderTableDao.save(groupedTable2());
        return tableGroup;
    }
}