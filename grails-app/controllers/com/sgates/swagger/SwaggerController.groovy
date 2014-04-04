package com.sgates.swagger

class SwaggerController {

    def index(){
        render swaggerService.api as JSON
    }
}
