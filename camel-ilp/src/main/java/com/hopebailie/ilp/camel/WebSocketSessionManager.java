package com.hopebailie.ilp.camel;

import com.hopebailie.ilp.Account;

import java.util.HashMap;
import java.util.Map;

//TODO Expire sessions after inactivity
public class WebSocketSessionManager {

  //Map of WebSocket Connection ID -> Account
  private final Map<String, Account> accountByConnection;
  private final Map<Account, String> connectionByAccount;


  public WebSocketSessionManager() {
    accountByConnection = new HashMap<>();
    connectionByAccount = new HashMap<>();
  }

  public Account getAccount(String connectionId) {
    return accountByConnection.get(connectionId);
  }

  public String getConnectionId(Account account) {
    return connectionByAccount.get(account);
  }

  public void createSession(String connectionId, Account account) {
    //TODO deal with conflicts
    accountByConnection.put(connectionId, account);
    connectionByAccount.put(account, connectionId);
  }

  public void refreshSession(String connectionId) {
    //TODO
  }

  public Account deleteSession(String connectionId) {
    Account account = accountByConnection.remove(connectionId);
    if(account != null) {
      connectionByAccount.remove(account);
    }
    return account;
  }

}
