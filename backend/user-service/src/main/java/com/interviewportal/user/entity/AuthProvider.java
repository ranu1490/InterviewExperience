package com.interviewportal.user.entity;

/**
 * How an account was created. Lets us treat local (password) and federated (Google) users
 * differently — e.g. a Google user has no local password to reset.
 */
public enum AuthProvider {
    LOCAL,
    GOOGLE
}
