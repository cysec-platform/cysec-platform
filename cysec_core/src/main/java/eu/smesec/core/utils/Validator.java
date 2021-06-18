package eu.smesec.core.utils;

import eu.smesec.bridge.generated.User;
import eu.smesec.core.exceptions.ValidationException;

import java.util.regex.Pattern;

public final class Validator {
    private static final Pattern regexWord = Pattern.compile("\\w*");
    private static final Pattern regexWordSpace = Pattern.compile("[\\w ]*");
    private static final Pattern regexEmail = Pattern.compile("\\w+(\\.\\w+)*\\@\\w+(\\.\\w+)+");
    private static final Pattern regexAnswer = Pattern.compile("[^\\/\\>\\<\\;\\?\\*\\!\\&\\{\\}]+");

    private Validator() {
    }

    /**
     * Checks if the input contains only word character: a-zA-Z_0-9.
     *
     * @param word input to check
     * @return <code>true</code> if the input contains only word character, or <code>false</code>
     * otherwise
     */
    public static boolean validateWord(String word) {
        return word != null && regexWord.matcher(word).matches();
    }

    /**
     * Checks if the input contains only word character: a-zA-Z_ 0-9.
     *
     * @param word input to check
     * @return <code>true</code> if the input contains only word character, or <code>false</code>
     * otherwise
     */
    public static boolean validateWordSpace(String word) {
        return word != null && regexWordSpace.matcher(word).matches();
    }

    /**
     * Checks if the mail address is valid.
     *
     * @param email email address to check
     * @return <code>true</code> if the email address is valid, or <code>false</code> otherwise
     */
    public static boolean validateEmail(String email) {
        return email != null && regexEmail.matcher(email).matches();
    }

    /**
     * Checks if the input contains non of the characters: /&gt;&lt;;?*!&amp;{} .
     *
     * @param input input to check
     * @return <code>true</code> if the input contains non of the specified characters, or <code>false
     * </code> otherwise
     */
    public static boolean validateAnswer(String input) {
        return input != null && regexAnswer.matcher(input).matches();
    }

    /**
     * Checks if an user object is valid.
     *
     * @param user user object
     */

    //FIXME add UserValidationException and more specific message
    public static void validateUser(User user) throws ValidationException {
        if (!validateWord(user.getUsername())) {
            throw new ValidationException("Could not validate Username");
        }
        if (!validateEmail(user.getEmail())) {
            throw new ValidationException("Could not validate email address");
        }
        if (!validateWordSpace(user.getFirstname())) {
            throw new ValidationException("Could not validate first name");
        }
        if (!validateWordSpace(user.getSurname())) {
            throw new ValidationException("Could not validate surname");
        }
        if (!user.getRole().stream().allMatch(Validator::validateWord)) {
            throw new ValidationException("Could not validate role");
        }

    }
}
