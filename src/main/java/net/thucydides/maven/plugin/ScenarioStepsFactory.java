package net.thucydides.maven.plugin;

import net.thucydides.jbehave.ThucydidesStepFactory;
import net.thucydides.jbehave.reflection.Extract;
import net.thucydides.maven.plugin.generate.FieldsSteps;
import net.thucydides.maven.plugin.generate.MethodArgument;
import net.thucydides.maven.plugin.generate.ScenarioMethod;
import net.thucydides.maven.plugin.generate.StepMethod;
import net.thucydides.maven.plugin.model.ScenarioStepsClassModel;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.parsers.StepMatcher;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.StepCandidate;
import org.jbehave.core.steps.StepCreator;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.thucydides.maven.plugin.utils.NameUtils.*;

public class ScenarioStepsFactory extends ThucydidesStepFactory {

    private String rootPackage;
    private List<StepCandidate> stepCandidates;
    private Map<String, Integer> argumentNames;

    private StepMethod stepMethod;
    private Set<String> imports;

    String[] parameterNames;
    private List<MethodArgument> methodArguments;
    StepMatcher stepMatcher;
    List<ParameterConverters.ParameterConverter> converters;

    public ScenarioStepsFactory(String rootPackage, ClassLoader classLoader) {
        super(new MostUsefulConfiguration(), rootPackage, classLoader);
        this.rootPackage = rootPackage;
    }

    private List<StepCandidate> findStepCandidates() {
        List<StepCandidate> stepCandidates = new ArrayList<StepCandidate>();
        for (CandidateSteps candidateSteps : createCandidateSteps()) {
            List<StepCandidate> stepCandidateList = candidateSteps.listCandidates();
            System.out.println(stepCandidateList.get(0));
            stepCandidates.addAll(stepCandidateList);
        }
        return stepCandidates;
    }

    public List<StepCandidate> getStepCandidates() {
        if (stepCandidates == null) {
            stepCandidates = findStepCandidates();
        }
        return stepCandidates;
    }

    public ScenarioStepsClassModel createScenarioStepsClassModelFrom(Story story) {
        System.out.println("ScenarioStepsClassModel creating for: " + story.getName());
        ScenarioStepsClassModel scenarioStepsClassModel = new ScenarioStepsClassModel();
        scenarioStepsClassModel.setPackageName(rootPackage);
        scenarioStepsClassModel.setClassNamePrefix(getClassNameFrom(story.getName()));
        imports = new HashSet<String>();
        Set<FieldsSteps> fieldSteps = new HashSet<FieldsSteps>();
        List<ScenarioMethod> scenarios = new ArrayList<ScenarioMethod>();

        matchStepsByStoryScenarios(story, scenarios, fieldSteps);

        scenarioStepsClassModel.setImports(imports);
        scenarioStepsClassModel.setFieldsSteps(fieldSteps);
        scenarioStepsClassModel.setScenarios(scenarios);

        return scenarioStepsClassModel;
    }

    private void matchStepsByStoryScenarios(Story story, List<ScenarioMethod> scenarios, Set<FieldsSteps> fieldSteps) {
        for (Scenario scenario : story.getScenarios()) {
            System.out.println("SCENARIO: " + scenario.getTitle());
            ScenarioMethod scenarioMethod = new ScenarioMethod();
            scenarioMethod.setScenarioName(scenario.getTitle());
            scenarioMethod.setMethodName(getMethodNameFrom(scenario.getTitle()));
            List<MethodArgument> scenarioMethodArguments = new LinkedList<MethodArgument>();
            scenarioMethod.setScenarioParameters(parseScenarioArguments(scenario.getTitle()));
            Set<String> thrownExceptions = new HashSet<String>();
            argumentNames = new HashMap<String, Integer>();
            List<StepMethod> stepMethods = new ArrayList<StepMethod>();

            matchStepsByStorySteps(scenario, scenarioMethodArguments, thrownExceptions, fieldSteps, stepMethods);

            scenarioMethod.setStepMethods(stepMethods);
            List<MethodArgument> methodArguments = resolveScenarioParameters(scenarioMethodArguments, scenarioMethod.getScenarioParameters());
            scenarioMethod.setArguments(methodArguments);
            scenarioMethod.setThrownExceptions(thrownExceptions);
            scenarios.add(scenarioMethod);
        }
    }

    private void matchStepsByStorySteps(Scenario scenario, List<MethodArgument> scenarioMethodArguments, Set<String> thrownExceptions, Set<FieldsSteps> fieldSteps, List<StepMethod> stepMethods) {

        String previousNonAndStep = null;

        for (String step : scenario.getSteps()) {
            System.out.println("STEP: " + step.toString());
            StepMethod matchedStepMethod = getMatchedStepMethodFor(step, previousNonAndStep, scenarioMethodArguments, thrownExceptions);
            if (matchedStepMethod.getMethodName() == null) {
                continue;
            }
            FieldsSteps fieldsSteps = new FieldsSteps();
            fieldsSteps.setClassName(matchedStepMethod.getMethodClass().getSimpleName());
            fieldsSteps.setFieldName(matchedStepMethod.getFieldName());
            fieldSteps.add(fieldsSteps);
            stepMethods.add(matchedStepMethod);
            if (!step.startsWith(Keywords.AND)) {
                previousNonAndStep = step;
            }
        }
    }

    private List<MethodArgument> resolveScenarioParameters(List<MethodArgument> scenarioMethodArguments, List<String> scenarioParameters) {
        List<MethodArgument> methodArgumentList = new LinkedList<MethodArgument>();
        for (String scenarioParameter : scenarioParameters) {
            methodArgumentList.add(findMethodArgumentByName(scenarioMethodArguments, scenarioParameter));
        }
        for (MethodArgument methodArgument : scenarioMethodArguments) {
            if (!methodArgumentList.contains(methodArgument)) {
                methodArgumentList.add(methodArgument);
            }
        }
        return methodArgumentList;
    }

    private MethodArgument findMethodArgumentByName(List<MethodArgument> scenarioMethodArguments, String name) {
        for (MethodArgument methodArgument : scenarioMethodArguments) {
            if (name.equals(methodArgument.getArgumentName())) {
                methodArgument.setArgumentDefaultValue(name);
                return methodArgument;
            }
        }
        MethodArgument methodArgument = new MethodArgument();
        methodArgument.setArgumentName(name);
        methodArgument.setArgumentClass(String.class);
        methodArgument.setArgumentType(String.class.getSimpleName());
        methodArgument.setArgumentDefaultValue(name);
        return methodArgument;
    }

    private List<String> parseScenarioArguments(String str) {
        Pattern p = Pattern.compile("\\$(\\w+)");
        Matcher m = p.matcher(str);
        List<String> arguments = new LinkedList<String>();
        while (m.find()) {
            arguments.add(m.group(1));
        }
        return arguments;
    }

    public StepMethod getMatchedStepMethodFor(String step, String previousNonAndStep, List<MethodArgument> scenarioMethodArguments, Set<String> thrownExceptions) {
        System.out.println("Matching step");
        stepMethod = new StepMethod();
        for (StepCandidate candidate : getStepCandidates()) {
            System.out.println("CANDIDATE: " + candidate.toString());
            if (candidate.matches(step, previousNonAndStep)) {
                stepMethod.setMethodName(candidate.getMethod().getName());
                Class<?>[] exceptionTypes = candidate.getMethod().getExceptionTypes();
                for (Class<?> exceptionType : exceptionTypes) {
                    thrownExceptions.add(exceptionType.getSimpleName());
                    imports.add(exceptionType.getCanonicalName());
                }

                setParametersToStepMethodAndStepCandidate(candidate);

                for (int i = 0; i < parameterNames.length; i++) {
                    MethodArgument methodArgument = new MethodArgument();
                    String parameterName = getUniqueParameterName(parameterNames[i]);
                    methodArgument.setArgumentName(parameterName);
                    String parameterValueAsString = stepMatcher.parameter(i + 1);
                    Class<?> argumentClass = candidate.getMethod().getParameterTypes()[i];
                    methodArgument.setArgumentClass(argumentClass);
                    methodArgument.setArgumentType(argumentClass.getSimpleName());
                    addToImports(imports, argumentClass);
                    Type type = candidate.getMethod().getGenericParameterTypes()[i];
                    methodArgument.setArgumentDefaultValue(convert(parameterValueAsString, type, converters, imports));
                    if (isParametrized(type)) {
                        String parametrizedTypeClassCanonicalName = getParametrizedTypeClassCanonicalName(type);
                        Class<?> parametrizedTypeClass = null;
                        try {
                            parametrizedTypeClass = getClass().getClassLoader().loadClass(parametrizedTypeClassCanonicalName);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                        methodArgument.setArgumentGenericType(parametrizedTypeClass.getSimpleName());
                        addToImports(imports, parametrizedTypeClass);
                    }
                    methodArguments.add(methodArgument);
                    scenarioMethodArguments.add(methodArgument);
                }
                stepMethod.setMethodArguments(methodArguments);
                return stepMethod;
            }
        }
        return stepMethod;
    }

    private void setParametersToStepMethodAndStepCandidate(StepCandidate candidate) {
        Class<?> stepClass = candidate.getMethod().getDeclaringClass();
        stepMethod.setMethodClass(stepClass);
        imports.add(stepClass.getCanonicalName());
        String fieldClassName = stepClass.getSimpleName();
        String fieldName = replaceFirstCharacterToLowerCase(fieldClassName);
        stepMethod.setFieldName(fieldName);
        //get method arguments
        methodArguments = new ArrayList<MethodArgument>();
        StepCandidate stepCandidate = (StepCandidate) Extract.field("stepCandidate").from(candidate);
        stepMatcher = (StepMatcher) Extract.field("stepMatcher").from(stepCandidate);

        StepCreator stepCreator = (StepCreator) Extract.field("stepCreator").from(stepCandidate);
        ParameterConverters parameterConverters = (ParameterConverters) Extract.field("parameterConverters").from(stepCreator);
        converters = (List<ParameterConverters.ParameterConverter>) Extract.field("converters").from(parameterConverters);

        parameterNames = stepMatcher.parameterNames();
    }

    private void addToImports(Set<String> imports, Class<?> argumentClass) {
        if (!argumentClass.isPrimitive()) {
            imports.add(argumentClass.getCanonicalName());
        }
    }

    public String getParametrizedTypeClassCanonicalName(Type type) {
        Type argumentType = argumentType(type);
        return argumentType.toString().replaceFirst("class ", "");
    }

    private boolean isParametrized(Type type) {
        return type instanceof ParameterizedType;
    }

    private Type rawType(Type type) {
        return ((ParameterizedType) type).getRawType();
    }

    private Type argumentType(Type type) {
        return ((ParameterizedType) type).getActualTypeArguments()[0];
    }

    private String getUniqueParameterName(String parameterName) {
        int counter = 0;
        if (argumentNames.containsKey(parameterName)) {
            counter = argumentNames.get(parameterName);
            counter++;
            argumentNames.put(parameterName, counter);
            parameterName = parameterName + counter;
        } else {
            argumentNames.put(parameterName, counter);
        }
        return parameterName;
    }

    public String convert(String value, Type type, List<ParameterConverters.ParameterConverter> converters, Set<String> imports) {
        // check if any converters accepts type
        for (ParameterConverters.ParameterConverter converter : converters) {
            if (converter.accept(type)) {
                Object converted = converter.convertValue(value, type);
                if (converted.getClass().equals(Integer.class) || converted.getClass().equals(Double.class)) {
                    return value;
                }
                addToImports(imports, converted.getClass());
                addToImports(imports, converter.getClass());
                if (converted.getClass().equals(ExamplesTable.class)) {
                    return "new " + converted.getClass().getSimpleName() + "(\"" + StringEscapeUtils.escapeJava(value) + "\")";
                }
                return "(" + converted.getClass().getSimpleName() + ") new " + converter.getClass().getSimpleName() + "().convertValue(\"" + StringEscapeUtils.escapeJava(value) + "\", new " + converted.getClass().getSimpleName() + "().getClass())";
            }
        }

        if (type == String.class) {
            return "\"" + StringEscapeUtils.escapeJava(value) + "\"";
        }

        throw new ParameterConverters.ParameterConvertionFailed("No parameter converter for " + type);
    }
}
