package org.traccar.web.i18n.rebind;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.google.gwt.core.ext.*;
import com.google.gwt.core.ext.linker.GeneratedResource;
import com.google.gwt.core.ext.typeinfo.*;
import com.google.gwt.i18n.client.Messages;
import com.google.gwt.i18n.rebind.AbstractResource;
import com.google.gwt.i18n.rebind.ResourceFactory;
import com.google.gwt.i18n.shared.GwtLocale;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

import java.io.*;
import java.util.Collection;
import java.util.MissingResourceException;

public class LocalizableGenerator extends Generator {
    final com.google.gwt.i18n.rebind.LocalizableGenerator gwtGenerator = new com.google.gwt.i18n.rebind.LocalizableGenerator();

    protected static UnableToCompleteException error(TreeLogger logger,
                                                     Throwable e) {
        logger.log(TreeLogger.ERROR, e.getMessage(), e);
        return new UnableToCompleteException();
    }

    protected static UnableToCompleteException error(TreeLogger logger, String msg) {
        logger.log(TreeLogger.ERROR, msg, null);
        return new UnableToCompleteException();
    }

    @Override
    public String generate(TreeLogger logger, GeneratorContext context, String typeName) throws UnableToCompleteException {
        TypeOracle oracle = context.getTypeOracle();
        JClassType constantsClass;
        JClassType messagesClass;
        JClassType targetClass;
        try {
            constantsClass = oracle.getType(com.google.gwt.i18n.rebind.LocalizableGenerator.CONSTANTS_NAME);
            messagesClass = oracle.getType(com.google.gwt.i18n.rebind.LocalizableGenerator.MESSAGES_NAME);
            targetClass = oracle.getType(typeName);
        } catch (NotFoundException e) {
            // Should never happen in practice.
            throw error(logger, e);
        }

        // Make sure the interface being rebound extends either Constants or
        // Messages.
        boolean assignableToConstants = constantsClass.isAssignableFrom(targetClass);
        boolean assignableToMessages = messagesClass.isAssignableFrom(targetClass);
        if (!assignableToConstants && !assignableToMessages) {
            // Let the implementation generator handle this interface.
            return gwtGenerator.generate(logger, context, typeName);
        }

        SourceWriter src = getSourceWriter(targetClass, context, logger);
        String resource = "i18n/" + targetClass.getQualifiedSourceName().replaceAll("\\.", "/");
        if (src != null) {
            src.println("private final StringTranslator strings = new StringTranslator(\"" + resource + "\");");

            JMethod[] methods = targetClass.getMethods();
            for (JMethod m : methods) {
                JParameter[] params = m.getParameters();
                String decl = m.getReadableDeclaration(false, true, true, true, true);
                src.print(decl);
                src.println("{");
                src.indent();
                if (params != null && params.length > 0 && params[0].isAnnotationPresent(Messages.Select.class)) {
                    src.print("return strings.translateSelect(");
                } else {
                    src.print("return strings.translate(");
                }
                src.print("\"" + m.getName() + "\"");
                if (params != null) {
                    for (JParameter p : params) {
                        src.print(", " + p.getName());
                    }
                }
                src.println(");");
                src.outdent();
                src.println("}");
            }

            src.commit(logger);
        }

        // write all messages resources
        LocaleInfoGenerator.LocaleParameters localeParams;
        try {
            localeParams = LocaleInfoGenerator.getLocaleParameters(context.getPropertyOracle(), logger);
        } catch (BadPropertyValueException e) {
            throw error(logger, e);
        }
        JsonFactory jsonFactory = new JsonFactory();
        for (GwtLocale locale : localeParams.allLocales) {
            AbstractResource.ResourceList resourceList = null;
            try {
                resourceList = ResourceFactory.getBundle(logger, targetClass, locale,
                        assignableToConstants, context);
                OutputStream os = context.tryCreateResource(logger, resource + "/" + locale.toString() + ".json");
                if (os != null) {
                    JsonGenerator jg = jsonFactory.createGenerator(os, JsonEncoding.UTF8);
                    jg.writeStartObject();
                    for (String key : resourceList.keySet()) {
                        AbstractResource.ResourceEntry entry = resourceList.getEntry(key);
                        Collection<String> forms = entry.getForms();
                        if (forms.isEmpty()) {
                            jg.writeStringField(key, resourceList.getString(key));
                        } else {
                            jg.writeObjectFieldStart(key);
                            jg.writeStringField("null", resourceList.getString(key));
                            for (String form : forms) {
                                jg.writeStringField(form, entry.getForm(form));
                            }
                            jg.writeEndObject();
                        }
                    }
                    jg.writeEndObject();
                    jg.flush();
                    GeneratedResource generatedResource = context.commitResource(logger, os);
                    context.commitArtifact(logger, generatedResource);
                }
            } catch (MissingResourceException e) {
                throw error(logger,
                        "Localization failed; there must be at least one resource accessible through"
                                + " the classpath in package '" + targetClass.getPackage().getName()
                                + "' whose base name is '"
                                + ResourceFactory.getResourceName(targetClass) + "'");
            } catch (IllegalArgumentException e) {
                // A bad key can generate an illegal argument exception.
                throw error(logger, e.getMessage());
            } catch (IOException e) {
                throw error(logger, e);
            }
        }

        return typeName + "Generated";
    }

    public SourceWriter getSourceWriter(JClassType classType, GeneratorContext context, TreeLogger logger) {
        String packageName = classType.getPackage().getName();
        String simpleName = classType.getSimpleSourceName() + "Generated";
        ClassSourceFileComposerFactory composer = new ClassSourceFileComposerFactory(packageName, simpleName);
        composer.addImplementedInterface(classType.getQualifiedSourceName());

        // Need to add whatever imports your generated class needs.
        composer.addImport("org.traccar.web.i18n.client.StringTranslator");

        PrintWriter printWriter = context.tryCreate(logger, packageName, simpleName);
        if (printWriter == null) { // already exists.
            return null;
        } else {
            return composer.createSourceWriter(context, printWriter);
        }
    }
}
