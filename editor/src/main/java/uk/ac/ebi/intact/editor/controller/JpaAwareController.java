/**
 * Copyright 2010 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.editor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.core.persister.CorePersister;
import uk.ac.ebi.intact.jami.dao.IntactDao;
import uk.ac.ebi.intact.jami.interceptor.AfterCommitExecutor;
import uk.ac.ebi.intact.jami.service.ComplexService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;


/**
 * IntAct JSF Base Controller.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public abstract class JpaAwareController extends BaseController {

    @PersistenceContext( unitName = "intact-core-default" )
    private EntityManager coreEntityManager;

    @Autowired
    private IntactContext intactContext;

    @Autowired
    private DaoFactory daoFactory;

    @Autowired
    @Qualifier("complexService")
    private ComplexService complexService;

    @Autowired
    @Qualifier("intactDao")
    private IntactDao intactDao;

    @Autowired
    @Qualifier("afterCommitExecutor")
    private AfterCommitExecutor afterCommitExecutor;

    protected EntityManager getCoreEntityManager() {
        return coreEntityManager;
    }

    public DaoFactory getDaoFactory() {
        return daoFactory;
    }

    protected IntactContext getIntactContext() {
        return intactContext;
    }

    protected CorePersister getCorePersister() {
        return getIntactContext().getCorePersister();
    }

    protected EntityManager getJamiEntityManager() {
        return intactDao.getEntityManager();
    }

    public ComplexService getComplexService() {
        return complexService;
    }

    public IntactDao getIntactDao() {
        return intactDao;
    }

    public AfterCommitExecutor getAfterCommitExecutor() {
        return afterCommitExecutor;
    }
}