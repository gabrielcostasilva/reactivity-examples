package contracts

import org.springframework.cloud.contract.spec.Contract
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType


Contract.make {
    description("should return all Movies")
    request {
        url ("/movies")
        method GET()
    }
    response {
        body(
            [
                [id: 1, title: "Hello World", genre: "Horror"],
                [id: 2, title: "Look at me", genre: "Drama"]
            ]
        )
        status(HttpStatus.OK.value())
        headers{
            contentType(MediaType.APPLICATION_JSON_VALUE)
        }
    }


}