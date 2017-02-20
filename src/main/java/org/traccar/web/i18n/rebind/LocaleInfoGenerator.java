/*
 * Copyright 2015 Vitaly Litvak (vitavaque@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.web.i18n.rebind;

import com.google.gwt.codegen.server.CodeGenUtils;
import com.google.gwt.core.ext.*;
import com.google.gwt.core.ext.impl.ResourceLocatorImpl;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.i18n.client.impl.LocaleInfoImpl;
import com.google.gwt.i18n.server.GwtLocaleFactoryImpl;
import com.google.gwt.i18n.server.GwtLocaleImpl;
import com.google.gwt.i18n.shared.GwtLocale;
import com.google.gwt.i18n.shared.GwtLocaleFactory;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;
import org.apache.tapestry.util.text.LocalizedProperties;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Generator used to generate an implementation of the LocaleInfoImpl class, which is used by the
 * LocaleInfo class.
 */
public class LocaleInfoGenerator extends Generator {
    private static final GwtLocaleFactoryImpl factory = new GwtLocaleFactoryImpl();

    /**
     * The token representing the locale property controlling Localization.
     */
    // @VisibleForTesting
    static final String PROP_LOCALE = "traccar.locale";

    /**
     * The config property identifying the URL query paramter name to possibly get
     * the value of the locale property.
     */
    // @VisibleForTesting
    static final String PROP_LOCALE_QUERY_PARAM = "locale.queryparam";

    /**
     * The config property identifying the cookie name to possibly get the value
     * of the locale property.
     */
    // @VisibleForTesting
    static final String PROP_LOCALE_COOKIE = "locale.cookie";

    /**
     * Properties file containing machine-generated locale display names, in their
     * native locales (if possible).
     */
    private static final String GENERATED_LOCALE_NATIVE_DISPLAY_NAMES = "com/google/gwt/i18n/client/impl/cldr/LocaleNativeDisplayNames-generated.properties";

    /**
     * Properties file containing hand-made corrections to the machine-generated
     * locale display names above.
     */
    private static final String MANUAL_LOCALE_NATIVE_DISPLAY_NAMES = "com/google/gwt/i18n/client/impl/cldr/LocaleNativeDisplayNames-manual.properties";

    /**
     * Properties file containing hand-made overrides of locale display names, in
     * their native locales (if possible).
     */
    private static final String OVERRIDE_LOCALE_NATIVE_DISPLAY_NAMES = "com/google/gwt/i18n/client/impl/cldr/LocaleNativeDisplayNames-override.properties";

    /**
     * Generate an implementation for the given type.
     *
     * @param logger error logger
     * @param context generator context
     * @param typeName target type name
     * @return generated class name
     * @throws UnableToCompleteException
     */
    @Override
    public final String generate(TreeLogger logger, final GeneratorContext context,
                                 String typeName) throws UnableToCompleteException {
        TypeOracle typeOracle = context.getTypeOracle();
        JClassType targetClass;
        try {
            targetClass = typeOracle.getType(typeName);
        } catch (NotFoundException e) {
            logger.log(TreeLogger.ERROR, "No such type " + typeName, e);
            throw new UnableToCompleteException();
        }
        assert (LocaleInfoImpl.class.getName().equals(
                targetClass.getQualifiedSourceName()));

        String packageName = targetClass.getPackage().getName();
        String className = targetClass.getName().replace('.', '_') + "Generated";

        PrintWriter pw = context.tryCreate(logger, packageName, className);
        if (pw != null) {
            PropertyOracle propertyOracle = context.getPropertyOracle();
            LocaleParameters params;
            try {
                params = getLocaleParameters(propertyOracle, logger);
            } catch (BadPropertyValueException e) {
                logger.log(TreeLogger.ERROR, "Unable to parse properties", e);
                throw new UnableToCompleteException();
            }

            Set<GwtLocale> localeSet = params.allLocales;
            GwtLocaleImpl[] allLocales = localeSet.toArray(new GwtLocaleImpl[localeSet.size()]);
            // sort for deterministic output
            Arrays.sort(allLocales);
            LocalizedProperties displayNames = new LocalizedProperties();
            LocalizedProperties displayNamesManual = new LocalizedProperties();
            LocalizedProperties displayNamesOverride = new LocalizedProperties();
            try {
                InputStream str = ResourceLocatorImpl.tryFindResourceAsStream(logger,
                        context.getResourcesOracle(), GENERATED_LOCALE_NATIVE_DISPLAY_NAMES);
                if (str != null) {
                    displayNames.load(str, "UTF-8");
                }
                str = ResourceLocatorImpl.tryFindResourceAsStream(logger, context.getResourcesOracle(),
                        MANUAL_LOCALE_NATIVE_DISPLAY_NAMES);
                if (str != null) {
                    displayNamesManual.load(str, "UTF-8");
                }
                str = ResourceLocatorImpl.tryFindResourceAsStream(logger, context.getResourcesOracle(),
                        OVERRIDE_LOCALE_NATIVE_DISPLAY_NAMES);
                if (str != null) {
                    displayNamesOverride.load(str, "UTF-8");
                }
            } catch (UnsupportedEncodingException e) {
                // UTF-8 should always be defined
                logger.log(TreeLogger.ERROR, "UTF-8 encoding is not defined", e);
                throw new UnableToCompleteException();
            } catch (IOException e) {
                logger.log(TreeLogger.ERROR, "Exception reading locale display names",
                        e);
                throw new UnableToCompleteException();
            }

            ClassSourceFileComposerFactory factory = new ClassSourceFileComposerFactory(
                    packageName, className);
            factory.setSuperclass(targetClass.getQualifiedSourceName());
            factory.addImport("com.google.gwt.core.client.GWT");
            factory.addImport("com.google.gwt.core.client.JavaScriptObject");
            factory.addImport("com.google.gwt.i18n.client.LocaleInfo");
            factory.addImport("com.google.gwt.i18n.client.constants.NumberConstants");
            factory.addImport("com.google.gwt.i18n.client.constants.NumberConstantsImpl");
            factory.addImport("com.google.gwt.i18n.client.DateTimeFormatInfo");
            factory.addImport("com.google.gwt.i18n.client.impl.cldr.DateTimeFormatInfoImpl");
            factory.addImport("java.util.HashMap");
            factory.addImport("org.traccar.web.i18n.client.LocaleProvider");

            SourceWriter writer = factory.createSourceWriter(context, pw);
            writer.println("private final LocaleProvider localeProvider = new LocaleProvider(\"" +
                    params.queryParam + "\", \"" + params.cookie + "\");");
            writer.println("private static native String getLocaleNativeDisplayName(");
            writer.println("    JavaScriptObject nativeDisplayNamesNative,String localeName) /*-{");
            writer.println("  return nativeDisplayNamesNative[localeName];");
            writer.println("}-*/;");
            writer.println();
            writer.println("HashMap<String,String> nativeDisplayNamesJava;");
            writer.println("private JavaScriptObject nativeDisplayNamesNative;");
            writer.println();
            writer.println("@Override");
            writer.println("public String[] getAvailableLocaleNames() {");
            writer.println("  return new String[] {");
            for (GwtLocaleImpl possibleLocale : allLocales) {
                writer.println("    \""
                        + possibleLocale.toString().replaceAll("\"", "\\\"") + "\",");
            }
            writer.println("  };");
            writer.println("}");
            writer.println();
            writer.println("@Override");
            writer.println("public String getLocaleNativeDisplayName(String localeName) {");
            writer.println("  if (GWT.isScript()) {");
            writer.println("    if (nativeDisplayNamesNative == null) {");
            writer.println("      nativeDisplayNamesNative = loadNativeDisplayNamesNative();");
            writer.println("    }");
            writer.println("    return getLocaleNativeDisplayName(nativeDisplayNamesNative, localeName);");
            writer.println("  } else {");
            writer.println("    if (nativeDisplayNamesJava == null) {");
            writer.println("      nativeDisplayNamesJava = new HashMap<String, String>();");
            {
                for (GwtLocaleImpl possibleLocale : allLocales) {
                    String localeName = possibleLocale.toString();
                    String displayName = displayNamesOverride.getProperty(localeName);
                    if (displayName == null) {
                        displayName = displayNamesManual.getProperty(localeName);
                    }
                    if (displayName == null) {
                        displayName = displayNames.getProperty(localeName);
                    }
                    if (displayName != null && displayName.length() != 0) {
                        writer.println("      nativeDisplayNamesJava.put("
                                + CodeGenUtils.asStringLiteral(localeName) + ", "
                                + CodeGenUtils.asStringLiteral(displayName) + ");");
                    }
                }
            }

            writer.println("    }");
            writer.println("    return nativeDisplayNamesJava.get(localeName);");
            writer.println("  }");
            writer.println("}");
            writer.println();
            writer.println("@Override");
            writer.println("public boolean hasAnyRTL() {");
            writer.println("  return false;");
            writer.println("}");
            writer.println();
            writer.println("private native JavaScriptObject loadNativeDisplayNamesNative() /*-{");
            writer.println("  return {");
            {
                boolean needComma = false;
                for (GwtLocaleImpl possibleLocale : allLocales) {
                    String localeName = possibleLocale.toString();
                    String displayName = displayNamesOverride.getProperty(localeName);
                    if (displayName == null) {
                        displayName = displayNamesManual.getProperty(localeName);
                    }
                    if (displayName == null) {
                        displayName = displayNames.getProperty(localeName);
                    }
                    if (displayName != null && displayName.length() != 0) {
                        if (needComma) {
                            writer.println(",");
                        }
                        writer.print("    " + CodeGenUtils.asStringLiteral(localeName) + ": "
                                + CodeGenUtils.asStringLiteral(displayName));
                        needComma = true;
                    }
                }
                if (needComma) {
                    writer.println();
                }
            }
            writer.println("  };");
            writer.println("}-*/;");
            writer.println("@Override");
            writer.println("public String getLocaleName() {");
            writer.println("  return localeProvider.getLocale();");
            writer.println("}");
            writer.println();
            String queryParam = params.queryParam;
            if (queryParam != null) {
                writer.println("@Override");
                writer.println("public String getLocaleQueryParam() {");
                writer.println("  return " + CodeGenUtils.asStringLiteral(queryParam) + ";");
                writer.println("}");
                writer.println();
            }
            String cookie = params.cookie;
            if (cookie != null) {
                writer.println("@Override");
                writer.println("public String getLocaleCookieName() {");
                writer.println("  return " + CodeGenUtils.asStringLiteral(cookie) + ";");
                writer.println("}");
                writer.println();
            }
            writer.println("@Override");
            writer.println("public DateTimeFormatInfo getDateTimeFormatInfo() {");
            LocalizableGenerator localizableGenerator = new LocalizableGenerator();
            // Avoid warnings for trying to create the same type multiple times
            generateConstantsLookup(logger, context, writer, localizableGenerator,
                    "com.google.gwt.i18n.client.impl.cldr.DateTimeFormatInfoImpl");
            writer.println("}");
            writer.println();
            writer.println("@Override");
            writer.println("public NumberConstants getNumberConstants() {");
            generateConstantsLookup(logger, context, writer, localizableGenerator,
                    "com.google.gwt.i18n.client.constants.NumberConstantsImpl");
            writer.println("}");
            writer.commit(logger);
        }
        return packageName + "." + className;
    }

    private void generateConstantsLookup(TreeLogger logger,
                                         GeneratorContext context, SourceWriter writer,
                                         LocalizableGenerator localizableGenerator,
                                         String typeName)
            throws UnableToCompleteException {
        writer.indent();
        generateOneLocale(logger, context, localizableGenerator, typeName);
        writer.println("return GWT.create(" + typeName + ".class);");
        writer.outdent();
    }

    private void generateOneLocale(TreeLogger logger, GeneratorContext context,
                                   LocalizableGenerator localizableGenerator, String typeName)
            throws UnableToCompleteException {
        String generatedClass = localizableGenerator.generate(logger, context, typeName);
        if (generatedClass == null) {
            logger.log(TreeLogger.ERROR, "Failed to generate " + typeName);
            // skip failed locale
        }
    }

    public static class LocaleParameters {
        final Set<GwtLocale> allLocales;
        final String queryParam;
        final String cookie;

        LocaleParameters(Set<GwtLocale> allLocales, String queryParam, String cookie) {
            this.allLocales = Collections.unmodifiableSet(allLocales);
            this.queryParam = queryParam;
            this.cookie = cookie;
        }
    }

    static LocaleParameters getLocaleParameters(PropertyOracle propertyOracle, TreeLogger logger) throws BadPropertyValueException{

        ConfigurationProperty localeProp
                = propertyOracle.getConfigurationProperty(PROP_LOCALE);
        ConfigurationProperty queryParamProp
                = propertyOracle.getConfigurationProperty(PROP_LOCALE_QUERY_PARAM);
        ConfigurationProperty cookieProp
                = propertyOracle.getConfigurationProperty(PROP_LOCALE_COOKIE);

        Set<GwtLocale> allLocales = new HashSet<>();
        String queryParam = queryParamProp.getValues().get(0);
        if (queryParam.length() == 0) {
            queryParam = null;
        }
        String cookie = cookieProp.getValues().get(0);
        if (cookie.length() == 0) {
            cookie = null;
        }
        List<String> localeValues = localeProp.getValues();

        GwtLocaleFactory factoryInstance = getLocaleFactory();
        for (String localeValue : localeValues) {
            allLocales.add(factoryInstance.fromString(localeValue));
        }
        return new LocaleParameters(allLocales, queryParam, cookie);
    }
    /**
     * Get a shared GwtLocale factory so instances are cached between all uses.
     *
     * @return singleton GwtLocaleFactory instance.
     */
    public static GwtLocaleFactory getLocaleFactory() {
        return factory;
    }
}