/**
 * Created with IntelliJ IDEA.
 * User: Steffen Gates
 * Date: 4/16/14
 * Time: 11:36 AM
 */
class SwaggerUrlMappings {
    static mappings = {
        "/swagger"(controller:"swagger", action: "index")
        "/swagger/api(.$format)?"(controller:"swagger", action:"api")
        "/swagger/api/$path?(.$format)?"(controller: "swagger", action:"resource")
        "/swaggerDocs"(controller:"swagger", action:"docs")
    }
}