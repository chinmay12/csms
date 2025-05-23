# CSMS service

CSMS system is used to authenticate drivers who wish to initiate charging.


## Technical Design


1. CSMS system contains two service viz. Transaction service and Authentication service.
2. Transaction service is responsible to handle the client/drivers charging authorisation requests.
3. Authentication service is responsible to authenticate the drivers information using pid and unique token generated by chargepoint application used by the drivers.
4. Transaction service is responsible for validating the payload data which is part authorisation request <application_hostname>/transaction/authorize. Transaction service has table named drivers which stores information about Table: drivers(driver_id, driver_name).
5. A request is made to <application_hostname>/transaction/authorize by client application. For sense of simplicity and security the token is encrypted using a symmetric key which is shared between the client and authentication server. The token is nothing but initial 16 digits SHA256(driver_id) encrypted by using the symmetric key.
6. The request payload is as below
```
API: <application_hostname>/transaction/authorize

{
    "stationUuid": "<uuid of station>"
    "driverIdentifier": {"id": "<identifier-string>",
     "token": <token value>                     
    }
}
```

5. Once all the validation are successful the transaction service adds a authentication request object to kafka topic named auth_requests.

The authentication request event payload looks like below

```
{
    requestId: <UUID>,
    type: "AuthenticationRequest",
    payload: {
        driverId:,
        token:
    }

}
```
6. Authentication serivce is listening the to new events added to topic auth_requests. On receiving the request the authentication service validates the token with tokens stored against driverID in the table drivers_tokens. Table details: driver_tokens(driverId, token)
7. Once the validation is done by the authentication service a authentication response event is added by it to kafka topic auth-responses. 

The authentication response event looks like below

```
{
    requestId: <UUID>,
    type: "AuthenticationResponse",
    payload: {
        driverId:,
        authenticationResult
    }

}

```

8. The transaction service listens to events on the topic auth_events. We have used Future with a predefined timeout to listen to auth response events in transaction service. As soon as it receives the event it gives a response back to client application with relevant response status.

9. The implementation has handled the scenarios of kakfa not available, authentication service is down or not responding withing time limits gracefully.


## Additional details

1. The drivers table is seeded with some data using flyway migration.
2. The drivers_tokens table is seeded with some driver_tokens data using flyway migration.

## Getting Started

Instructions to get the application up and running on local setup.

### Prerequisites

Install following list of software

Docker engine

Github cli

### Deploying the service on local setup

A step by step series of examples that tell you how to get a development env running


1. Clone github repository using command

```
gh repo clone chinmay12/csms

```
2. Browse to folder where folder is copied in the previous step.
```
   cd <Location of provisioning folder in your machine>
```
```
3. 
export ENCRYPTION_PASSWORD=chargepoint12345
docker compose build
```
```
4. docker compose up
```

```
5. Application successfully started logs

transaction-service: partitions assigned: [auth-responses-0, auth-responses-1, auth-responses-2, auth-responses-3, auth-responses-4, auth-responses-5, auth-responses-6, auth-responses-7, auth-responses-8, auth-responses-9]

authentication-service: partitions assigned: [auth-requests-0, auth-requests-1, auth-requests-2, auth-requests-3, auth-requests-4, auth-requests-5, auth-requests-6, auth-requests-7, auth-requests-8, auth-requests-9]


```

## API details

URL at which application is available: http://localhost:8081/

1. API to authorise charging session

```
HTTP Method: POST

Header: Content-Type:application/json

URI: /transaction/authorize

Request Body:

{
    "stationUuid": "<uuid of station>"
    "driverIdentifier": {"id": "<identifier-string>",
     "token": <token value>                     
    }
}
Response:

HTTP status code 200

Body:
{
    "authorizationStatus": "Accepted"
}


HTTP status code 401

Body:
{
    "authorizationStatus": "Rejected"
}



HTTP status code 400

Body:
{
    "authorizationStatus": "Invalid"
}



HTTP status code 404

Body:
{
    "authorizationStatus": "Unknown"
}


HTTP status code 500

Body:
{
    "authorizationStatus": "Rejected"
}
 
```

Test suite

Test scenario 1: Valid driver id and token

Curl requests

```

curl -i --request POST \
  --url http://localhost:8081/transaction/authorize \
   --header 'Content-Type: application/json' \
  --header 'User-Agent: insomnia/11.0.2' \
  --data '{
	"stationUUID": "test",
	
	"driverIdentifier": {
		"id": "k39fjqv7zpxa28dmslwug1hy",
		"token": "fEM2SgSqhi4ierRn/o5x9pTAwhlMOuUCksf8IzoOTFk="
	}	
}'


curl -i --request POST \
  --url http://localhost:8081/transaction/authorize \
  --header 'Content-Type: application/json' \
  --header 'User-Agent: insomnia/11.0.2' \
  --data '{
	"stationUUID": "test",
	"driverIdentifier":{ 
		"id":"d7lvmnqfhwz39eupxk62btg4c",
		"token": "qOKfVipNvWmQjEe7n3ymf5TAwhlMOuUCksf8IzoOTFk="
	}	
}'


```

Test scenario 2: Valid driver id and invalid token

```
curl -i --request POST \
  --url http://localhost:8081/transaction/authorize \
  --header 'Content-Type: application/json' \
  --header 'User-Agent: insomnia/11.0.2' \
  --data '{
	"stationUUID": "test",
	"driverIdentifier":{ 
		"id":"d7lvmnqfhwz39eupxk62btg4c",
		"token": "qOKfVipNvWmQjEe7n3ymf5TAwhlMOuUCksf8IzuUTFk="
	}	
}'

```

Test scenario 3: Unknown driver id

```

curl -i --request POST \
  --url http://localhost:8081/transaction/authorize \
  --header 'Content-Type: application/json' \
  --header 'User-Agent: insomnia/11.0.2' \
  --data '{
	"stationUUID": "test",
	"driverIdentifier":{ 
		"id":"d7lvmnqfhwz39eupxk62cqg4c",
		"token": "qOKfVipNvWmQjEe7n3ymf5TAwhlMOuUCksf8IzuUTFk="
	}	
}'


```

Test scenario 4: Invalid driver id

```

curl -i --request POST \
  --url http://localhost:8081/transaction/authorize \
  --header 'Content-Type: application/json' \
  --header 'User-Agent: insomnia/11.0.2' \
  --data '{
	"stationUUID": "test",
	"driverIdentifier":{ 
		"id":"test1234",
		"token": "qOKfVipNvWmQjEe7n3ymf5TAwhlMOuUCksf8IzuUTFk="
	}	
}'


```

Test sceanario 5: Kafka is down

You may kill the kafka docker process.


Test scenario 6: Authentication service does not response within timeout interval interval. Currently, I have kept timeout to 20 seconds



## Seed data details. 

You can use the below driver id and token combinations while testing the application

driver_id: k39fjqv7zpxa28dmslwug1hy
token: fEM2SgSqhi4ierRn/o5x9pTAwhlMOuUCksf8IzoOTFk=
driver_id: d7lvmnqfhwz39eupxk62btg4c
token: qOKfVipNvWmQjEe7n3ymf5TAwhlMOuUCksf8IzoOTFk=
driver_id: qpva2tx84fduo1cyzmnrsljwhb
token: fg9vQzjn7AyOKedqA+qnL5TAwhlMOuUCksf8IzoOTFk=
driver_id: xmye2h7rgsl3vkw9ufpda1tnb
token: hTyM4Clfz3YXNvsbr93YIZTAwhlMOuUCksf8IzoOTFk=
driver_id: b9vznc5xaplhqy2wutsdkm3oje
token: 4BwM2ZyQ6PgS0xmJBzmxIZTAwhlMOuUCksf8IzoOTFk=





## Built With

* Spring boot: Application framework(Faster development)
* Maven: Dependency management
* H2: Embeded database
* Flyway: Database migration tool
* CI/CD: Docker
* Kafka

## Code repository



## Authors

Chinmay Nalawade email: nalawade.chinmay@gmail.com
# csms
