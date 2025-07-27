package com.radovicdanilo.pixelwar.config.security

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class CheckSecurity(val roles: Array<Roles> = [])
