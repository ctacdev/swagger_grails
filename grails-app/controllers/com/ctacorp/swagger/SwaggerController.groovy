package com.ctacorp.swagger

import grails.converters.JSON
import org.markdown4j.Markdown4jProcessor

class SwaggerController {
    def swaggerService

    def index(){
        render view:grailsApplication.config.swagger.customView ?: "index"
    }

    def api(){
        String resp = swaggerService.api as JSON
        if(params.callback) {
            resp = params.callback + "(" + resp + ");"
        }
        render resp
    }

    def resource(String path){
        if(path){
            String resp = swaggerService.getResourceDetails("/${path}") as JSON
            if(params.callback) {
                resp = params.callback + "(" + resp + ");"
            }
            render resp
            return
        }
    }

    def docs(boolean raw) {
        String md = swaggerService.getSwaggerMarkdown()
        if(!raw){
            String h = new Markdown4jProcessor().process(md)
            render view:'docs', model:[html:h]
            return
        }
        render md
    }
}
