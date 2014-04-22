<!DOCTYPE html>
<html>
<head>
    <title>Swagger UI</title>
    <r:require module="swaggerUI"/>
    <r:layoutResources />
    <script type="text/javascript">
        $(function () {
            window.swaggerUi = new SwaggerUi({
                url: "${createLink(controller: 'swagger', action:'api', absolute: true)}",
                dom_id: "swagger-ui-container",
                supportedSubmitMethods: ['get', 'post', 'put', 'delete'],
                onComplete: function (swaggerApi, swaggerUi) {
                    log("Loaded SwaggerUI");

                    if (typeof initOAuth == "function") {
                        /*
                         initOAuth({
                         clientId: "your-client-id",
                         realm: "your-realms",
                         appName: "your-app-name"
                         });
                         */
                    }
                    $('pre code').each(function (i, e) {
                        hljs.highlightBlock(e)
                    });
                },
                onFailure: function (data) {
                    log("Unable to Load SwaggerUI");
                },
                docExpansion: "none"
            });

            $('#input_apiKey').change(function () {
                var key = $('#input_apiKey')[0].value;
                log("key: " + key);
                if (key && key.trim() != "") {
                    log("added key " + key);
                    window.authorizations.add("key", new ApiKeyAuthorization("api_key", key, "query"));
                }
            })
            window.swaggerUi.load();
        });
    </script>
</head>

<body>
<div id='header'>
    <div class="swagger-ui-wrap">
        <a id="logo" href="http://swagger.wordnik.com">swagger</a>

        <form id='api_selector'>
            <div class='input icon-btn'>
                <r:img id="show-pet-store-icon" dir="images/swagger/pet_store_api.png" title="Show Swagger Petstore Example Apis"/>
            </div>

            <div class='input icon-btn'>
                <r:img id="show-wordnik-dev-icon" dir="images/swagger/wordnik_api.png" title="Show Wordnik Developer Apis"/>
            </div>

            <div class='input'><input placeholder="http://example.com/api" id="input_baseUrl" name="baseUrl" type="text"/></div>

            <div class='input'><input placeholder="api_key" id="input_apiKey" name="apiKey" type="text"/></div>

            <div class='input'><a id="explore" href="#">Explore</a></div>
        </form>
    </div>
</div>

<div id="message-bar" class="swagger-ui-wrap">&nbsp;</div>

<div id="swagger-ui-container" class="swagger-ui-wrap"></div>
<r:layoutResources />
</body>
</html>
