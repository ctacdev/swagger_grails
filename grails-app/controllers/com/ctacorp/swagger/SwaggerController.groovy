package com.ctacorp.swagger

class SwaggerController {

    def index(){
        render swaggerService.api as JSON
    }
}
