/**
 * Copyright 2007 The European Bioinformatics Institute, and others.
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
 *  limitations under the License.
 */
package uk.ac.ebi.intact.services.validator;

/**
 * Validator view exception.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id: ValidatorViewException.java 1482 2012-10-18 11:18:29Z noedelta $
 */
public class ValidatorViewException extends RuntimeException
{

    public ValidatorViewException()
    {
        super();
    }

    public ValidatorViewException(String message)
    {
        super(message);
    }

    public ValidatorViewException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ValidatorViewException(Throwable cause)
    {
        super(cause);
    }
}
