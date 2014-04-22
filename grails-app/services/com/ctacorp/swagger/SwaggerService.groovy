package com.ctacorp.swagger

import com.ctacorp.grails.swagger.annotations.*
import grails.converters.JSON
import groovy.json.JsonSlurper

class SwaggerService {
    static transactional = false

    def grailsApplication

    static allowedPrimitives = ["integer","number","string","boolean"]

    def getApi() {

        def authorizations = [:]

        def api = [
            apiVersion:grailsApplication.config.swagger.apiVersion,
            swaggerVersion:grailsApplication.config.swagger.swaggerVersion,
            info:grailsApplication.config.swagger.info,
            authorizations:authorizations
        ]

        def apis = []

        grailsApplication.controllerClasses.each{ controllerArtifact ->
            def controllerClass = controllerArtifact.clazz
            controllerClass.annotations.each { annotation ->
                if(annotation.annotationType() == API){
                    apis << [path:annotation.swaggerDataPath(), description:annotation.description()]
                }
            }
        }

        apis = apis.sort{ it.path }

        api.apis = apis
        api
    }

    private getController(String controllerName){
        grailsApplication.mainContext.getBean(controllerName)
    }

    def findController(String path){
        def controller = null
        grailsApplication.controllerClasses.each{ controllerArtifact ->
            def controllerClass = controllerArtifact.clazz
            for(annotation in controllerClass.annotations){
                if(annotation.annotationType() == API){
                    if(annotation.swaggerDataPath() == path){
                        controller = getController(controllerClass.name)
                    }
                }
            }
        }
        controller
    }

    private List getApis(controller){
        def apis = []
        def alreadyMapped = [:]

        controller.class.methods.each{ method ->
            if(method.isAnnotationPresent(APIResource) && !alreadyMapped["${method.name}"]){
                alreadyMapped["${method.name}"] = true
                APIResource annotation = method.getAnnotation(APIResource)
                def api = [
                    description:annotation.description(),
                    path:annotation.path()
                ]
                def operations = []
                annotation.operations().each { Operation o ->
                    def responseMessages = []
                    o.responseMessages().each{ ResponseMessage e ->
                        responseMessages << [code:e.code(), message: e.description(), responseModel: e.type()]
                    }
                    def parameters = []
                    o.parameters().each { Parameter p ->
                        def parametersAttrs = [
                            allowMultiple:p.allowMultiple(),
                            description: p.description(),
                            name:p.name(),
                            type:p.type(),
                            paramType:p.paramType(),
                            required:p.required(),
                            defaultValue:p.defaultValue()
                        ]
                        def format = p.format()
                        if(format) {
                            parametersAttrs.format = format
                        }
                        parameters << parametersAttrs
                    }

                    def operation = [
                        responseMessages: responseMessages,
                        method: o?.httpMethod(),
                        nickname: o?.nickname(),
                        notes: o?.notes(),
                        summary: o?.summary(),
                        produces: o?.produces(),
                        consumes: [o?.consumes()]
                    ]

                    def responseType = o?.type()
                    if(responseType) {
                        operation.type = responseType
                    }

                    def typeRef = o?.typeRef()

                    if(typeRef) {
                        operation.items = [:]
                        if(allowedPrimitives.contains(typeRef)) {
                            operation.items.type = typeRef
                        } else {
                            operation.items."\$ref" = typeRef
                        }
                    }

                    def format = o?.format()
                    if(format) {
                        if(typeRef) {
                            operation.items.format = format
                        } else {
                            operation.format = format
                        }
                    }

                    if(parameters){
                        operation.parameters = parameters
                    }
                    operations << operation
                }
                api.operations = operations
                apis << api
            }
        }
        apis.sort{ it.path }
    }

    private Map getModels(controller){

        def models = [:]
        def processedModels = [:]

        def addModel = { id, properties ->
            def model = [id: id, properties: properties.properties]
            if(properties.required) {
                model.required = properties.required
            }
            models[id] = model
        }

        def convertModel = { Model model ->

            def required = []
            def modelProperties = model.properties()
            def properties = getProperties(modelProperties)
            addModel(model.id(), properties)
            processedModels."${model.id()}" = model
        }

        def addModelExtension = { ModelExtension modelExtension ->

            def modelExtensionId = modelExtension.id()
            def modelId = modelExtension.model()
            def model = processedModels[modelId] as Model

            if(model) {

                def propertyMap = extendProperties(model, modelExtension)
                def properties = getProperties(propertyMap.values())

                if(!modelExtensionId) {
                    models.remove(modelExtensionId)
                    addModel(modelId, properties)
                } else {
                    addModel(modelExtensionId, properties)
                }
            }
        }

        API classAnnotation = controller.class.getAnnotation(API)

        classAnnotation.models().each { Model model ->
            convertModel model
        }

        classAnnotation.modelRefs().each { Class modelRef ->

            def model = modelRef.getAnnotation(Model)
            if(model) {
                convertModel model
            }

            def modelExtension = modelRef.getAnnotation(ModelExtension)
            if(modelExtension) {
                addModelExtension modelExtension
            }
        }

        classAnnotation.modelExtensions().each { ModelExtension modelExtension ->
            addModelExtension modelExtension
        }

        models
    }

    private Map getProperties(modelProperties) {

        def props = [:]
        def required = [] as List<String>

        modelProperties.each { ModelProperty modelProperty ->

            def attrs = [:]
            modelProperty.attributes().each { PropertyAttribute attr ->

                attrs.type = attr?.type()

                if (attr.required()) {
                    required.add(modelProperty.propertyName())
                }

                def typeRef = attr?.typeRef()
                if (typeRef) {
                    attrs.items = [:]
                    if(allowedPrimitives.contains(typeRef)) {
                        attrs.items.type = typeRef
                    } else {
                        attrs.items."\$ref" = typeRef
                    }
                }

                def format = attr?.format()
                if(format) {
                    if(typeRef) {
                        attrs.items.format = format
                    } else {
                        attrs.format = format
                    }
                }
            }
            props << ["${modelProperty.propertyName()}": attrs]
        }

        [properties: props, required: required]
    }

    private Map extendProperties(Model model, ModelExtension modelExtension) {

        def propertyMap = [:]

        (model.properties() as ModelProperty[]).each {
            propertyMap[it.propertyName()] = it
        }

        modelExtension.removeProperties().each {
            if(propertyMap.containsKey(it)) {
                propertyMap.remove(it)
            }
        }

        modelExtension.addProperties().each {
            if(!propertyMap.containsKey(it.propertyName())) {
                propertyMap[it.propertyName()] = it
            }
        }

        propertyMap
    }

    Map getResourceDetails(String resourcePath){
        def resourceBlock = [
            resourcePath:resourcePath,
            basePath:grailsApplication.config.swagger.api.basePath,
            apiVersion:grailsApplication.config.swagger.apiVersion,
            swaggerVersion:grailsApplication.config.swagger.swaggerVersion
        ]

        def controller = findController(resourcePath)

        resourceBlock.apis = getApis(controller)
        resourceBlock.models = getModels(controller)

        resourceBlock
    }

    def getSwaggerMarkdown(){
        JsonSlurper js = new JsonSlurper()
        def apiJson = js.parseText((this.api as JSON).toString())
        String md = "#${apiJson.info.title}\n###API Details"
        md +=
            """
**API Version** ${apiJson.apiVersion}
**Swagger Version** ${apiJson.swaggerVersion}
**Contact** ${apiJson.info.contact}
**License** ${apiJson.info.license}
**License Url** [${apiJson.info.licenseUrl}](${apiJson.info.licenseUrl})
**License Url** [${apiJson.info.termsOfServiceUrl}](${apiJson.info.termsOfServiceUrl})

${apiJson.info.description}\n
"""
        md += "###API Listing\n"
        apiJson.apis.each{ api ->
            md += "####Resource: ${api.path}\n"
            def apiDetails = getResourceDetails(api.path)
            md += "**Description** ${api.description}\n"
            md += "**Base Path** ${apiDetails.basePath}\n"

            md += "#####End Points\n"
            apiDetails.apis.each{ endPoint ->
                md += "**Path** ${endPoint.path}\n"
                md += "**Description** ${endPoint.description}\n"
                md += "**Operations**\n"
                endPoint.operations.each{ op ->
                    md += "**Method** ${op.method}\n"
                    md += "**Nickname** ${op.nickname}\n"
                    md += "**Notes** ${op.notes}\n"
                    md += "######Parameters\n"
                    op.parameters.each{ p ->
                        md += "* **name**: ${p.name}  \n"
                        md += "**required**: ${p.required}  \n"
                        md += "**description**: ${p.description}  \n"
                        md += "**paramType**: ${p.paramType}  \n"
                        md += "**type**: ${p.type}  \n"
                        md += "**format**: ${p.format}  \n"
                        md += "**defaultValue**: ${p.defaultValue}  \n"
                        md += "**allowMultiple**: ${p.allowMultiple}  \n\n"
                    }
                }
            }

            md += "\n---\n"
        }
        md
    }
}