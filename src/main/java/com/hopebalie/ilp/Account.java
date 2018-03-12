package com.hopebalie.ilp;

import org.interledger.annotations.Immutable;
import org.interledger.core.InterledgerAddress;

import javax.money.CurrencyUnit;
import java.util.Optional;

/**
 * An account between two Interledger peers.
 */
public interface Account extends Comparable<Account> {

  static AccountBuilder builder() {
    return new AccountBuilder();
  }

  /**
   * An local identifier for the account
   *
   * @return a UUID identifying the account
   */
  String getAccountId();

  /**
   * The currency unit of the asset underlying this account. (Despite the name of the returned
   * object, this value may represent a non-currency asset).
   *
   * @return A {@link CurrencyUnit}.
   */
  CurrencyUnit getCurrencyUnit();

  /**
   * <p>The order of magnitude to express one full currency unit in this account's base units. More
   * formally, an integer (..., -2, -1, 0, 1, 2, ...), such that one of the account's base units
   * equals 10^-<tt>currencyScale</tt> <tt>currencyCode</tt></p>
   *
   * <p>For example, if the integer values represented on the system are to be interpreted
   * as dollar-cents (for the purpose of settling a user's account balance, for instance), then the
   * account's currencyScale is 2. The amount 10000 would be translated visually into a decimal
   * format via the following equation: 10000 * (10^(-2)), or "100.00".</p>
   */
  Integer getCurrencyScale();

  /**
   * The relationship between this account and the local node.
   *
   * <p>When an Interledger node peers with another through an account they will establish a
   * relationship that can have one of three types depending on how they fit into the wider
   * network hierarchy.
   *
   * @return An {@link AccountRelationship}.
   */
  AccountRelationship getRelationship();

  /**
   * Address segment to use when generating an address for this account
   *
   * @return the address segment to append to this nodes address when generating an address for this account
   */
  Optional<InterledgerAddress> getAddressSegment();

  @Immutable
  abstract class AbstractAccount implements Account {

    @Override
    public int compareTo(Account otherAccount) {

      int compare = this.getRelationship().compareTo(otherAccount.getRelationship());

      if(compare == 0) {
        return this.getAccountId().compareTo(otherAccount.getAccountId());
      }

      return compare;
    }


    @Override
    public boolean equals(Object otherAccount) {
      if(otherAccount != null && otherAccount instanceof Account) {
        return getAccountId().equals(((Account) otherAccount).getAccountId());
      }
      return false;
    }
  }


}
