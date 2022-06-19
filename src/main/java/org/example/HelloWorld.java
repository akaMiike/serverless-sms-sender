package org.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

/**
 * Hello world!
 *
 */
public class HelloWorld implements RequestHandler<Object, Object> {

    @Override
    public Object handleRequest(Object input, Context context) {
        return null;
    }
}
