package com.sgates.swagger

import grails.transaction.Transactional

@Transactional
class SwaggerService {
    static transactional = false

    def grailsApplication

    def getApi() {
        def api = [
            apiVersion:grailsApplication.config.swagger.apiVersion,
            swaggerVersion:grailsApplication.config.swagger.swaggerVersion,
            basePath:grailsApplication.config.swagger.basePath
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
                    def errorCodes = []
                    o.errors().each{ ErrorCode e ->
                        errorCodes << [code:e.errorCode(), reason: e.description()]
                    }
                    def parameters = []
                    o.paramaters().each { Parameter p ->
                        parameters << [
                            allowMultiple:p.allowMultiple(),
                            dataType:p.dataType(),
                            description: p.description(),
                            name:p.name(),
                            paramType:p.paramType(),
                            required:p.required(),
                            defaultValue:p.defaultValue()
                        ]
                    }
                    def operation = [
                        errorResponses: errorCodes,
                        method: o?.httpMethod(),
                        nickname: o?.nickname(),
                        notes: o?.notes(),
                        responseClass: o?.responseClass(),
                        summary: o?.summary(),
                        produces: [o?.produces()],
                        consumes: [o?.consumes()]
                    ]
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

        API classAnnotation = controller.class.getAnnotation(API)
        classAnnotation.models().each { Model model ->
            def props = [:]
            model.properties().each { ModelProperty prop ->
                def attrs = [:]
                prop.attributes().each {PropertyAttribute attr ->
                    attrs."${attr.attribute()}" = attr.value()
                    attrs.required = attr.required()
                }
                props << ["${prop.propertyName()}":attrs]
            }
            models."${model.id()}" = [id:model.id(), properties:props]
        }

        models
    }

    private Map getResourceDetails(String resourcePath, String controllerName){
        def resourceBlock = [
            resourcePath:resourcePath,
            basePath:grailsApplication.config.swagger.api.basePath,
            apiVersion:grailsApplication.config.swagger.apiVersion,
            swaggerVersion:grailsApplication.config.swagger.swaggerVersion
        ]

        def controller = getController(controllerName)

        resourceBlock.apis = getApis(controller)
        resourceBlock.models = getModels(controller)

        resourceBlock
    }
}
