package io.surisoft.capi.controller;

import io.surisoft.capi.oidc.OIDCClientManager;
import io.surisoft.capi.oidc.OIDCException;
import io.surisoft.capi.schema.Group;
import io.surisoft.capi.schema.OIDCClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.camel.util.json.JsonArray;
import org.apache.camel.util.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@ConditionalOnProperty(prefix = "oidc.provider", name = "enabled", havingValue = "true")
@RequestMapping("/manager/client")
@Tag(name="Client Manager", description = "Manage OIDC/OAUTH2 Clients")
public class ClientController {
    private static final Logger log = LoggerFactory.getLogger(ClientController.class);
    @Autowired
    OIDCClientManager oidcClientManager;

    @Operation(summary = "Create an OIDC client")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Client created"),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
    @PostMapping
    public ResponseEntity<OIDCClient> registerClient(@RequestBody JsonObject oidcClient) {
        try {
            OIDCClient client = oidcClientManager.registerClient("capi-" + oidcClient.getString("name"));
            return new ResponseEntity<>(client, HttpStatus.OK);
        } catch (IOException | OIDCException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Subscribe to an API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Subscribed"),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
    @PostMapping("/{apiId}/{clientId}")
    public ResponseEntity<Void> subscribeApi(@PathVariable String apiId, @PathVariable String clientId) {
        try {
            oidcClientManager.assignServiceAccountRole(apiId, clientId);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (IOException | OIDCException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Get all CAPI clients")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All CAPI Clients"),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
    @GetMapping
    public ResponseEntity<List<OIDCClient>> getAllCapiClients() {
        try {
            return new ResponseEntity<>(oidcClientManager.getCapiClients(), HttpStatus.OK);
        } catch (IOException | OIDCException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Get all Groups")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All Groups"),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
    @GetMapping("/groups")
    public ResponseEntity<List<Group>> getAllGroups() {
        try {
            return new ResponseEntity<>(oidcClientManager.getGroups(), HttpStatus.OK);
        } catch (IOException | OIDCException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Add Client to Group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All Groups"),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
    @GetMapping("/group/{name}/client/{client}")
    public ResponseEntity<JsonArray> addClientToGroup(@PathVariable String name, @PathVariable String client) {
        try {
            if(!oidcClientManager.addClientToGroup(name, client)) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IOException | OIDCException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Get all clients of a Group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All Group clients"),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
    @GetMapping("/group/{name}/clients")
    public ResponseEntity<List<String>> addClientToGroup(@PathVariable String name) {
        try {
            return new ResponseEntity<>(oidcClientManager.getAllClientsOfGroup(name), HttpStatus.OK);
        } catch (IOException | OIDCException ex) {
            log.error(ex.getMessage(), ex);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}