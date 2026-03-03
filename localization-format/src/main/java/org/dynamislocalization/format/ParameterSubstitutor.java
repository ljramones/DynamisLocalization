package org.dynamislocalization.format;

import org.dynamis.core.logging.DynamisLogger;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves parameter tokens in localized string templates.
 *
 * Supported token forms:
 * - {@code {name}}        - simple substitution: replace with String.valueOf(params.get("name"))
 * - {@code {name, type}}  - formatted substitution: format value using LocaleAwareFormatter
 *
 * Example:
 * <pre>
 *   template: "You have {count} gold and {balance, currency} in the bank."
 *   params:   {count=42, balance=1234.50}
 *   result:   "You have 42 gold and $1,234.50 in the bank."
 * </pre>
 *
 * Unknown parameters are left as-is (token retained in output) with a debug log.
 * Unknown format types throw DynamisException (fail fast - indicates a template error).
 */
public final class ParameterSubstitutor {

    private static final DynamisLogger log = DynamisLogger.get(ParameterSubstitutor.class);

    /** Matches {name} and {name, type} tokens. */
    private static final Pattern TOKEN = Pattern.compile(
        "\\{([a-zA-Z_][a-zA-Z0-9_]*)(?:,\\s*([a-zA-Z]+))?\\}");

    private final LocaleAwareFormatter formatter;

    public ParameterSubstitutor(LocaleAwareFormatter formatter) {
        this.formatter = formatter;
    }

    /**
     * Substitutes all parameter tokens in the template using the provided params map.
     *
     * @param template the localized string template
     * @param params   named parameter values
     * @return the rendered string with all tokens replaced
     */
    public String substitute(String template, Map<String, Object> params) {
        if (template == null) return "";
        if (params == null || params.isEmpty()) return template;

        Matcher matcher = TOKEN.matcher(template);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String paramName = matcher.group(1);
            String formatType = matcher.group(2); // null if no format type

            Object value = params.get(paramName);
            if (value == null) {
                log.debug(String.format(
                    "Parameter '%s' not found in params map - token retained", paramName));
                matcher.appendReplacement(result,
                    Matcher.quoteReplacement(matcher.group(0)));
                continue;
            }

            String rendered;
            if (formatType != null) {
                rendered = formatter.format(value, formatType);
            } else {
                rendered = String.valueOf(value);
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(rendered));
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
