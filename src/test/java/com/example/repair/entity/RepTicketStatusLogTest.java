package com.example.repair.entity;

import com.example.repair.mapper.RepTicketStatusLogMapper;
import org.apache.ibatis.annotations.Insert;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class RepTicketStatusLogTest {

    @Test
    void shouldUseInsertSqlThatDoesNotReferenceUpdatedAt() throws NoSuchMethodException {
        Method method = RepTicketStatusLogMapper.class.getMethod("insertStatusLog", RepTicketStatusLog.class);
        Insert insert = method.getAnnotation(Insert.class);

        assertThat(insert).isNotNull();
        assertThat(String.join(" ", insert.value()))
                .contains("ticket_id")
                .doesNotContain("updated_at");
    }
}
