package com.ctacorp.swagger

import grails.converters.JSON
import org.markdown4j.Markdown4jProcessor

class SwaggerController {
    def swaggerService

    def index(){
        render view:grailsApplication.config.swagger.customView ?: "index"
    }

    def api(String path){
        render swaggerService.api as JSON
    }

    def resource(String path){
        if(path){
            render swaggerService.getResourceDetails("/${path}") as JSON
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
