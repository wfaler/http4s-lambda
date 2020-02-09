ioe# Http4s Lambda
Http4S bindings for AWS API Gateway &amp; Lambda

## Design goals
* Allow running a local Http4s server for a fast and productive development cycle. [Done]
* Allow running Http4s `Routes` to be used with no changes as an AWS Lambda (where the Lambda can be used as the target function for >1 API Gateway route). [Done]
* As a consequence of the two goals above, enable running Http4s as either a traditional JVM process, or as an AWS Lambda with no changes to code. [Done]
* Create an sbt plugin that makes publishing as easy as `sbt publishApi` [TODO]

## Usage
Easiest possible usage, lambda implemented like so:

    import cats.effect.IO
    import io.chaordic.aws.IOService
    import org.http4s.HttpRoutes
    import org.http4s.dsl.io._
    
    
    class LambdaHandler extends IOService{
    
      val routes = HttpRoutes.of[IO] {
          case GET -> Root / "hello" / name => Ok(s"Hello, $name!")
          case PUT -> Root / "hello" / name => Ok(s"Hello PUT, $name!")
          case PUT -> Root / "bye" / "bye" => Ok(s"Bye root")
        }
    }

Note that you can get your `HttpRoutes` from anywhere, and you can use other Monads than `IO`, we just provide a convenience trait for `IO` based routes.

The key things is to implement `trait io.chaordic.aws.IOService` and provide it with an implementation for `def routes: HttpRoutes`.

### Deployment
We will provide an Sbt plugin that makes this easier in the future, but for now:
* Create an AWS Lambda, and define a handler of `[fully qualified class name implementing IOService]::handle`
* Create an AWS API Gateway
* Create a resource of type "proxy" (`{proxy+} `), point it at your lambda.
* Deploy an API with the resource.

Done!
We will automate this in the future from Sbt..

The proxy resource means that any path and method request will go straight to your lambda, so it allows Http4s to deal with the routing.

### Acknowledgements
This code borrows from [Howard Johns Scala server lambda](https://github.com/howardjohn/scala-server-lambda), but intends to move in slightly different directions.
