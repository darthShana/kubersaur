package com.darthShana.kubersaur.generator.microservice;

import com.darthShana.kubersaur.cli.Language;
import com.darthShana.kubersaur.generator.microservice.api.MicroserviceApiGenerator;
import com.darthShana.kubersaur.generator.microservice.helm.HelmChartGenerator;
import com.darthShana.kubersaur.model.Microservice;
import com.darthShana.kubersaur.model.Org;
import org.kubersaur.codegen.implementation.CodegenConfig;

import java.io.File;
import java.io.IOException;
import java.util.ServiceLoader;

public class MicroserviceGenerator {

    private final String name;
    private final String implementationBaseDirectory;
    private final String interfaceBaseDirectory;
    private final String interfaceParentDir;
    private final String implementationParentDir;
    private final Language language;
    private Org org;

    private static ServiceLoader<CodegenConfig> implementationLoader = ServiceLoader.load(CodegenConfig.class);

    public MicroserviceGenerator(String name, Language language, Org org) {
        this.name = name;
        this.language = language;
        this.org = org;
        this.interfaceParentDir = "code/api/";
        this.implementationParentDir = "code/service/";
        this.implementationBaseDirectory = implementationParentDir+this.name+"-service/";
        this.interfaceBaseDirectory = interfaceParentDir+this.name+"-api/";

        implementationLoader.forEach(l->l.getName());
    }

    public void generate() throws IOException {
        org.addMicroservice(new Microservice(name, language));

        new File(implementationBaseDirectory).mkdirs();
        new File(interfaceBaseDirectory).mkdirs();
        new MicroserviceApiGenerator(name, interfaceBaseDirectory, interfaceParentDir, org).generate();

        String templateDir = implementationParentDir + language.getTemplateLocation();

        switch (language) {
            case JAVA:
                new com.darthShana.kubersaur.generator.microservice.service.java.MicroserviceImplGenerator(name, implementationBaseDirectory, templateDir, org).generate();
                break;
            case CSHARP:
                new com.darthShana.kubersaur.generator.microservice.service.csharp.MicroserviceImplGenerator(name, implementationBaseDirectory, templateDir, org).generate();
                break;

        }


        new ReactorPomGenerator(org, interfaceParentDir).generate();
        new ReactorPomGenerator(org, implementationParentDir).generate();

        new HelmChartGenerator(name, org).generate();

    }
}
