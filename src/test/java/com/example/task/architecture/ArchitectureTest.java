package com.example.task.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.stereotype.Repository;
import jakarta.persistence.Entity;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@AnalyzeClasses(packages = "com.example.task",
        importOptions = {ImportOption.DoNotIncludeTests.class})
public class ArchitectureTest {

    private static final String ROOT_PACKAGE = "com.example.task";
    private static final String CONTROLLER_PACKAGE = "..controller..";
    private static final String SERVICE_PACKAGE = "..service..";
    private static final String REPOSITORY_PACKAGE = "..repository..";
    private static final String ENTITY_PACKAGE = "..entity..";

    // === Grupa 1: Warstwy nie mogą sięgać w dół w niepoprawny sposób ===

    @ArchTest
    public static final ArchRule controllersShouldOnlyDependOnServicesOrThemselves =
            noClasses()
                    .that().resideInAPackage(CONTROLLER_PACKAGE)
                    .should().dependOnClassesThat().resideInAnyPackage(ENTITY_PACKAGE, REPOSITORY_PACKAGE)
                    .because("Kontrolery nie powinny bezpośrednio zależieć od encji ani repozytoriów");

    @ArchTest
    public static final ArchRule servicesShouldNotDependOnControllers =
            noClasses()
                    .that().resideInAPackage(SERVICE_PACKAGE)
                    .should().dependOnClassesThat().resideInAPackage(CONTROLLER_PACKAGE)
                    .because("Serwisy nie powinny zależeć od kontrolerów (odwrócenie zależności)");

    // === Grupa 2: Konwencje nazewnictwa i lokalizacji ===

    @ArchTest
    public static final ArchRule classesAnnotatedWithControllerShouldBeInControllerPackage =
            classes()
                    .that().areAnnotatedWith(Controller.class)
                    .should().resideInAPackage(CONTROLLER_PACKAGE)
                    .because("Klasy oznaczone @Controller muszą być w pakiecie controller");

    @ArchTest
    public static final ArchRule classesAnnotatedWithServiceShouldBeInServicePackage =
            classes()
                    .that().areAnnotatedWith(Service.class)
                    .should().resideInAPackage(SERVICE_PACKAGE)
                    .because("Klasy oznaczone @Service muszą być w pakiecie service");

    @ArchTest
    public static final ArchRule classesAnnotatedWithRepositoryShouldBeInRepositoryPackage =
            classes()
                    .that().areAnnotatedWith(Repository.class)
                    .should().resideInAPackage(REPOSITORY_PACKAGE)
                    .because("Klasy oznaczone @Repository muszą być w pakiecie repository");

    @ArchTest
    public static final ArchRule classesAnnotatedWithEntityShouldBeInEntityPackage =
            classes()
                    .that().areAnnotatedWith(Entity.class)
                    .should().resideInAPackage(ENTITY_PACKAGE)
                    .because("Klasy oznaczone @Entity muszą być w pakiecie entity");

    // === Bonus: Klasyczna architektura warstwowa (opcjonalnie, ale ładnie wygląda) ===

    @ArchTest
    public static final ArchRule layeredArchitectureRule =
            layeredArchitecture()
                    .consideringAllDependencies()
                    .layer("Controller").definedBy(CONTROLLER_PACKAGE)
                    .layer("Service").definedBy(SERVICE_PACKAGE)
                    .layer("Repository").definedBy(REPOSITORY_PACKAGE)
                    .layer("Entity").definedBy(ENTITY_PACKAGE)

                    .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
                    .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller")
                    .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service")
                    .whereLayer("Entity").mayOnlyBeAccessedByLayers("Repository", "Service")
                    .because("Powinniśmy przestrzegać klasycznej architektury warstwowej");
}