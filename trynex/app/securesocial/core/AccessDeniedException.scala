
package securesocial.core

/**
 * An exception thrown when the user denies access to the application
 * in the login page of the 3rd party service
 */
case class AccessDeniedException() extends Exception
