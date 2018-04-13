package com.hopebailie.ilp;

import org.interledger.annotations.Immutable;
import org.interledger.core.InterledgerAddress;

public interface InterledgerNodeContext {

  InterledgerAddress getAddress();

  @Immutable
  abstract class AbstractInterledgerNodeContext implements InterledgerAddress{

  }

}
