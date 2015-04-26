package com.ullink

import static org.junit.Assert.*
import org.junit.Test

class IKVMDocLetTest {
    @Test
    void "IKVMDocLet optionLength falls back to std JavaDoc options"() {
        assertEquals(1, IKVMDocLet.optionLength('-author'))
    }

    @Test
    void "IKVMDocLet optionLength should answer to IKVM specific options"() {
        assertEquals(2, IKVMDocLet.optionLength('-assembly'))
    }
}
