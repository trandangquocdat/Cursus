package com.fpt.cursus.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = Regex.class)
class RegexTest {

    @Autowired
    private Regex regex;

    @Test
    void testValidPhoneNumber() {
        assertTrue(regex.isPhoneValid("0774501962"));
        assertTrue(regex.isPhoneValid("+84774501962"));

        assertTrue(regex.isPhoneValid("0384978133"));
        assertTrue(regex.isPhoneValid("+84384978133"));

        assertTrue(regex.isPhoneValid("0971746413"));
        assertTrue(regex.isPhoneValid("+84971746413"));

        assertTrue(regex.isPhoneValid("0567890123"));
        assertTrue(regex.isPhoneValid("+84567890123"));

        assertTrue(regex.isPhoneValid("0887654321"));
        assertTrue(regex.isPhoneValid("+84887654321"));
    }

    @Test
    void testInvalidPhoneNumber() {
        assertFalse(regex.isPhoneValid("123456789"));
        assertFalse(regex.isPhoneValid("03212345"));
        assertFalse(regex.isPhoneValid("+841231234567"));
        assertFalse(regex.isPhoneValid("+840321234567"));
        assertFalse(regex.isPhoneValid("032a123456"));
        assertFalse(regex.isPhoneValid("0321 234 567"));
        assertFalse(regex.isPhoneValid("091123456"));
        assertFalse(regex.isPhoneValid("+84912345"));
    }
}
