package com.example.repair.entity;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RepTicketStatusLogTest {

    @Test
    void shouldExcludeUpdatedAtFromInsertColumns() {
        TableInfoHelper.remove(RepTicketStatusLog.class);

        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), StringPool.EMPTY);
        assistant.setCurrentNamespace(getClass().getName());

        TableInfo tableInfo = TableInfoHelper.initTableInfo(assistant, RepTicketStatusLog.class);

        assertThat(tableInfo.getAllInsertSqlColumnMaybeIf(null, false))
                .contains("ticket_id")
                .contains("created_at")
                .doesNotContain("updated_at");
    }
}
