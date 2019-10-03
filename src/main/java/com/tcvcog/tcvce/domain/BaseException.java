/*
 * Copyright (C) 2017 cedba
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.tcvcog.tcvce.domain;



/**
 *
 * @author cedba
 */
public class BaseException extends java.lang.Exception{
  private String message = "";
  private Exception exception = null;

  public BaseException()
  {
    super();
  }

  public BaseException(String message)
  {
    super();
    this.message = message;
    this.exception = null;
  }

  public BaseException(Exception e)
  {
    super();
    this.message = this.getClass().getName();
    this.exception = e;
  }

  public BaseException(String message, Exception e)
  {
    super();
    this.message = message;
    this.exception = e;
  }

  @Override
  public String getMessage()
  {
    if ( ( (message == null) || (message.length() == 0)) && exception != null)
    {
      return exception.getMessage();
    }
    else
    {
      return this.message;
    }
  }

  public Exception getException()
  {
    return exception;
  }

  @Override
  public String toString()
  {
    return getMessage();
  }

  @Override
  public void printStackTrace()
  {
    super.printStackTrace();
    if (exception != null)
    {
      System.err.println();
      System.err.println("Embedded exception:");
      exception.printStackTrace();
    }
  }

  @Override
  public void printStackTrace(java.io.PrintStream s)
  {
    super.printStackTrace(s);
    if (exception != null)
    {
      s.println();
      s.println("Embedded exception:");
      exception.printStackTrace(s);
    }
  }

  @Override
  public void printStackTrace(java.io.PrintWriter s)
  {
    super.printStackTrace(s);
    if (exception != null)
    {
      s.println();
      s.println("Embedded exception:");
      exception.printStackTrace(s);
    }
  }

}
