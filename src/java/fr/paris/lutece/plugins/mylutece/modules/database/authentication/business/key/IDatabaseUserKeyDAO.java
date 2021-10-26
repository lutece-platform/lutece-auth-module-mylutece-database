/*
 * Copyright (c) 2002-2021, City of Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.key;

import fr.paris.lutece.portal.service.plugin.Plugin;

/**
 *
 * IDatabaseUserKey
 *
 */
public interface IDatabaseUserKeyDAO
{
    /**
     * Load an instance of {@link DatabaseUserKey}
     * 
     * @param strKey
     *            the key
     * @param plugin
     *            the plugin
     * @return an instance of {@link DatabaseUserKey}
     */
    DatabaseUserKey load( String strKey, Plugin plugin );

    /**
     * Insert a new {@link DatabaseUserKey}
     * 
     * @param userKey
     *            the {@link DatabaseUserKey}
     * @param plugin
     *            the plugin
     */
    void insert( DatabaseUserKey userKey, Plugin plugin );

    /**
     * Delete a {@link DatabaseUserKey}
     * 
     * @param strKey
     *            the key
     * @param plugin
     *            the plugin
     */
    void delete( String strKey, Plugin plugin );

    /**
     * Delete by id user
     * 
     * @param nUserId
     *            the id user
     * @param plugin
     *            the plugin
     */
    void deleteByIdUser( int nUserId, Plugin plugin );

    /**
     * Find a key from a given login
     * 
     * @param login
     *            the user's login
     * @param plugin
     *            the plugin
     * @return a {@link DatabaseUserKey}
     */
    DatabaseUserKey selectKeyByLogin( String login, Plugin plugin );
}
