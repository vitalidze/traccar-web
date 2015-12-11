package org.traccar.web.i18n.linker;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.linker.ConfigurationProperty;
import com.google.gwt.core.ext.linker.PropertyProviderGenerator;
import com.google.gwt.user.rebind.SourceWriter;
import com.google.gwt.user.rebind.StringSourceWriter;

import java.util.SortedSet;
import java.util.regex.Pattern;

public class LocalePropertyProviderGenerator implements PropertyProviderGenerator {
    public static final String LOCALE_QUERYPARAM = "locale.queryparam";

    public static final String LOCALE_COOKIE = "locale.cookie";

    protected static final Pattern COOKIE_PATTERN = Pattern.compile("^[A-Za-z][A-Za-z0-9_]*$");

    protected static final Pattern QUERYPARAM_PATTERN = Pattern.compile("^[A-Za-z][A-Za-z0-9_]*$");

    @Override
    public String generate(TreeLogger logger, SortedSet<String> possibleValues,
                           String fallback, SortedSet<ConfigurationProperty> configProperties)
            throws UnableToCompleteException {
        // get relevant config property values
        String localeQueryParam = null;
        String localeCookie = null;
        String localeSearchOrder = "queryparam,cookie";
        for (ConfigurationProperty configProp : configProperties) {
            String name = configProp.getName();
            if (LOCALE_QUERYPARAM.equals(name)) {
                localeQueryParam = configProp.getValues().get(0);
                if (localeQueryParam != null && localeQueryParam.length() != 0
                        && !validateQueryParam(localeQueryParam)) {
                    logger.log(TreeLogger.WARN, "Ignoring invalid value of '"
                            + localeQueryParam + "' from '" + LOCALE_QUERYPARAM
                            + "', not a valid query parameter name");
                    localeQueryParam = null;
                }
            } else if (LOCALE_COOKIE.equals(name)) {
                localeCookie = configProp.getValues().get(0);
                if (localeCookie != null && localeCookie.length() != 0
                        && !validateCookieName(localeCookie)) {
                    logger.log(TreeLogger.WARN, "Ignoring invalid value of '"
                            + localeCookie + "' from '" + LOCALE_COOKIE
                            + "', not a valid cookie name");
                    localeCookie = null;
                }
            }
        }
        // provide a default for the search order
        if (fallback == null) {
            fallback = "default";
        }

        // build property provider body
        StringSourceWriter body = new StringSourceWriter();
        body.println("{");
        body.indent();
        body.println("var locale = null;");
        body.println("var rtlocale = '" + fallback + "';");
        body.println("try {");
        for (String method : localeSearchOrder.split(",")) {
            if ("queryparam".equals(method)) {
                if (localeQueryParam != null && localeQueryParam.length() > 0) {
                    body.println("if (!locale) {");
                    body.indent();
                    generateQueryParamLookup(logger, body, localeQueryParam);
                    body.outdent();
                    body.println("}");
                }
            } else if ("cookie".equals(method)) {
                if (localeCookie != null && localeCookie.length() > 0) {
                    body.println("if (!locale) {");
                    body.indent();
                    generateCookieLookup(logger, body, localeCookie);
                    body.outdent();
                    body.println("}");
                }
            } else {
                logger.log(TreeLogger.WARN, "Ignoring unknown locale lookup method \""
                        + method + "\"");
                body.println("// ignoring invalid lookup method '" + method + "'");
            }
        }
        body.println("if (!locale) {");
        body.indent();
        body.println("locale = $wnd['__gwt_Locale'];");
        body.outdent();
        body.println("}");
        body.println("if (locale) {");
        body.indent();
        body.println("rtlocale = locale;");
        body.outdent();
        body.println("}");
        body.outdent();
        body.println("} catch (e) {");
        body.indent();
        body.println("alert(\"Unexpected exception in locale detection, using "
                + "default: \" + e);\n");
        body.outdent();
        body.println("}");
        body.println("$wnd['__gwt_Locale'] = rtlocale;");
        body.println("return locale || \"" + fallback + "\";");
        body.outdent();
        body.println("}");
        return body.toString();
    }

    /**
     * Validate that a name is a valid cookie name.
     *
     * @return true if cookieName is an acceptable cookie name
     */
    protected boolean validateCookieName(String cookieName) {
        return COOKIE_PATTERN.matcher(cookieName).matches();
    }

    /**
     * Validate that a value is a valid query parameter name.
     *
     * @param queryParam
     * @return true if queryParam is a valid query parameter name.
     */
    protected boolean validateQueryParam(String queryParam) {
        return QUERYPARAM_PATTERN.matcher(queryParam).matches();
    }

    /**
     * Generate JS code to get the locale from a query parameter.
     *
     * @param logger logger to use
     * @param body where to append JS output
     * @param queryParam the query parameter to use
     * @throws UnableToCompleteException
     */
    protected void generateQueryParamLookup(TreeLogger logger, SourceWriter body,
                                            String queryParam) throws UnableToCompleteException  {
        body.println("var queryParam = location.search;");
        body.println("var qpStart = queryParam.indexOf(\"" + queryParam + "=\");");
        body.println("if (qpStart >= 0) {");
        body.indent();
        body.println("var value = queryParam.substring(qpStart + "
                + (queryParam.length() + 1) + ");");
        body.println("var end = queryParam.indexOf(\"&\", qpStart);");
        body.println("if (end < 0) {");
        body.indent();
        body.println("end = queryParam.length;");
        body.outdent();
        body.println("}");
        body.println("locale = queryParam.substring(qpStart + "
                + (queryParam.length() + 1) + ", end);");
        body.outdent();
        body.println("}");
    }

    /**
     * Generate JS code that looks up the locale value from a cookie.
     *
     * @param logger logger to use
     * @throws UnableToCompleteException
     */
    protected void generateCookieLookup(TreeLogger logger, SourceWriter body,
                                        String cookieName) throws UnableToCompleteException  {
        body.println("var cookies = $doc.cookie;");
        body.println("var idx = cookies.indexOf(\"" + cookieName + "=\");");
        body.println("if (idx >= 0) {");
        body.indent();
        body.println("var end = cookies.indexOf(';', idx);");
        body.println("if (end < 0) {");
        body.indent();
        body.println("end = cookies.length;");
        body.outdent();
        body.println("}");
        body.println("locale = cookies.substring(idx + " + (cookieName.length() + 1)
                + ", end);");
        body.outdent();
        body.println("}");
    }
}
