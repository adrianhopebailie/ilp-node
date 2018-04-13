package com.hopebailie.camel;


import org.apache.camel.Message;

import java.util.Map;

public interface Wrapper {

  Object wrap(Message message);

  Message unwrap(Object body);

}
