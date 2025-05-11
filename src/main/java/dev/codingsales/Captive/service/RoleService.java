package dev.codingsales.Captive.service;

import dev.codingsales.Captive.entity.Role;
import dev.codingsales.Captive.exeption.NoContentException;

public interface RoleService {
    public Role getRoleByName(String name) throws NoContentException;
}
