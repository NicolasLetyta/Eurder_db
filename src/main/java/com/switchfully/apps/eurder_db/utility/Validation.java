package com.switchfully.apps.eurder_db.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;
import java.util.function.Predicate;

public class Validation {

    private static final Logger logger = LoggerFactory.getLogger(Validation.class);

    private Validation() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static <T> T validateArgument(T argumentToValidate, String exceptionMessage, Predicate<T> invalidWhen) throws IllegalArgumentException {
        if (invalidWhen.test(argumentToValidate)) {
            logger.error(exceptionMessage);
            throw new IllegalArgumentException(exceptionMessage);
        }
        return argumentToValidate;
    }

    public static <T> T validateArgumentWithBooleanCondition(T argumentToValidate, String exceptionMessage, Boolean invalidWhen) throws IllegalArgumentException {
        if (invalidWhen) {
            logger.error(exceptionMessage);
            throw new IllegalArgumentException(exceptionMessage);
        }
        return argumentToValidate;
    }

    public static <T> T validateArgument(T argumentToValidate, String exceptionMessage, Predicate<T> invalidWhen, Function<String, RuntimeException> exceptionFunction) throws RuntimeException {
        if (invalidWhen.test(argumentToValidate)) {
            logger.error(exceptionMessage);
            throw exceptionFunction.apply(exceptionMessage);
        }
        return argumentToValidate;
    }

    public static <T> T validateArgumentWithBooleanCondition(T argumentToValidate, String exceptionMessage, Boolean invalidWhen, Function<String, RuntimeException> exceptionFunction) throws RuntimeException {
        if (invalidWhen) {
            logger.error(exceptionMessage);
            throw exceptionFunction.apply(exceptionMessage);
        }
        return argumentToValidate;
    }

    public static String validateNonBlank(String string, String exceptionMessage, Function<String, RuntimeException> exceptionFunction) throws RuntimeException {
        return validateArgumentWithBooleanCondition(string, exceptionMessage, string == null || string.isBlank(), exceptionFunction);
    }

    public static <T> T checkArgumentAndLogWarn(T argumentToValidate, String LogMessage, Predicate<T> warnWhen) {
        if (warnWhen.test(argumentToValidate)) {
            logger.warn(LogMessage);
        }
        return argumentToValidate;
    }

    public static <T> T logWarningIfConditionMet(T argumentToValidate, String LogMessage, boolean warnWhen) {
        if (warnWhen) {
            logger.warn(LogMessage);
        }
        return argumentToValidate;
    }
}
