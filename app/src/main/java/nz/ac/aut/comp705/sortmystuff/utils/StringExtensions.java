package nz.ac.aut.comp705.sortmystuff.utils;

public class StringExtensions {

    /**
     * Capitalise the first character of s. The output string will be trimmed first.
     *
     * @param s the string
     * @return a new string whose first character is upper case
     */
    public static String capitalise(String s) {
        if (s == null) return null;
        String output = s.trim();
        return output.substring(0, 1).toUpperCase() + output.substring(1);
    }
}
