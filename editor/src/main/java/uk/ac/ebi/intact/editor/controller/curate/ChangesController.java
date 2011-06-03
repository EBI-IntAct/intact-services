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
package uk.ac.ebi.intact.editor.controller.curate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.persister.IntactCore;
import uk.ac.ebi.intact.core.users.model.User;
import uk.ac.ebi.intact.core.util.DebugUtil;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.editor.controller.UserListener;
import uk.ac.ebi.intact.editor.controller.UserSessionController;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;

import java.util.*;

/**
 * Contains the information about current changes.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Component
public class ChangesController extends JpaAwareController implements UserListener {

    private static final Log log = LogFactory.getLog(ChangesController.class);

    /**
     * Map containing the user name as the key, and a list with his/her changes.
     */
    private Map<String,List<UnsavedChange>> changesPerUser;

    public ChangesController() {
        changesPerUser = new HashMap<String, List<UnsavedChange>>();
    }

    @Override
    public void userLoggedIn(User user) {
        if (user == null) return;

        changesPerUser.put(user.getLogin(), new ArrayList<UnsavedChange>());
    }

    @Override
    public void userLoggedOut(User user) {
        userLoggedOut(user.getLogin());
    }

    private void userLoggedOut(String user) {
        final List<UnsavedChange> unsavedChanges = getUnsavedChangesForUser(user);

        if (unsavedChanges == null) {
            throw new IllegalStateException("No unsaved changes found for user: "+user);
        }

        if (!unsavedChanges.isEmpty()) {
            if (log.isInfoEnabled()) log.info("User logged out with "+unsavedChanges.size()+" pending changes: "+user);

            unsavedChanges.clear();
        }

        removeUserFromUnsaved(user);
    }

    public void markAsUnsaved(IntactObject io) {
        if (io == null) return;

        if (io.getAc() != null) {
            addUnsavedChange(new UnsavedChange(io, UnsavedChange.UPDATED, null));
        } else {
            addUnsavedChange(new UnsavedChange(io, UnsavedChange.CREATED, null));
        }
    }

    public void markAsUnsaved(IntactObject io, Collection<String> parentAcs) {
        if (io == null) return;

        UnsavedChange change;

        if (io.getAc() != null) {
            change = new UnsavedChange(io, UnsavedChange.UPDATED, null);
        } else {
            change = new UnsavedChange(io, UnsavedChange.CREATED, null);
        }
        change.getAcsToDeleteOn().addAll(parentAcs);

        addUnsavedChange(change);
    }

    public void markToDelete(IntactObject object, AnnotatedObject parent) {
        if (object.getAc() != null) {

            String scope;

            if (parent != null && parent.getAc() != null){
                scope = parent.getAc();
            }
            else {
                scope = null;
            }

            // very important to delete all changes which can be affected by the delete of this object!!!
            removeObsoleteChangesOnDelete(object);

            addChange(new UnsavedChange(object, UnsavedChange.DELETED, parent, scope));

            // line commented because already done when adding the change
            //removeFromUnsaved(object);
        } else {
            AnnotatedObjectUtils.removeChild(parent, object);
        }

        /*if (parent != null && parent.getAc() != null) {
            addChange(new UnsavedChange(parent, UnsavedChange.UPDATED));
        }*/
    }

    /**
     * When deleting an object, all save/created/deleted events attached to one of the children of this object became obsolete because will be deleted with the current object
     * @param object
     */
    public void removeObsoleteChangesOnDelete(IntactObject object){
        if (object.getAc() != null){

            List<UnsavedChange> changes = new ArrayList(getUnsavedChangesForCurrentUser());
            for (UnsavedChange change : changes){

                if (change.getAcsToDeleteOn().contains(object.getAc())){
                    getUnsavedChangesForCurrentUser().remove(change);
                }
            }
        }
    }

    /**
     * When saving an object, all save/created/deleted events attached to one of the children of this object became obsolete because will be updated with the current object.
     * However, in case of new publication, new experiment, new interaction, new participant, new feature, it is important to keep the change as it will not be created while updating this event.
     * New objects are only attached to their parents if saved. So when saving the parent, it will not create the child because not added yet
     *
     * @param object
     */
    public void removeObsoleteChangesOnSave(IntactObject object){
        if (object.getAc() != null){

            List<UnsavedChange> changes = new ArrayList(getUnsavedChangesForCurrentUser());
            for (UnsavedChange change : changes){

                // very important to check that the ac is not null. Any new children event is not obsolete after saving the parent because not added yet
                if (change.getAcsToDeleteOn().contains(object.getAc()) && change.getUnsavedObject().getAc() != null){
                    getUnsavedChangesForCurrentUser().remove(change);
                }
            }
        }
    }

    public void markToCreatedTranscriptWithoutMaster(IntactObject object, AnnotatedObject parent) {
        String scope;

        if (parent != null && parent.getAc() != null){
            scope = parent.getAc();
        }
        else {
            scope = null;
        }
        addUnsavedChange(new UnsavedChange(object, UnsavedChange.CREATED_TRANSCRIPT, scope));
    }

    @Transactional
    public void markToDeleteInteraction(Interaction interaction, Collection<Experiment> experiments) {
        Collection<Experiment> parents;

        if (IntactCore.isInitialized(experiments)) {
            parents = experiments;
        } else {
            parents = getDaoFactory().getInteractionDao().getByAc(interaction.getAc()).getExperiments();
        }

        // using an array to avoid a concurrent modification exception, which happens when trying to remove the interaction from its experiments
        final Experiment[] array = parents.toArray(new Experiment[parents.size()]);

        for (int i=0; i<array.length; i++) {
            markToDelete(interaction, array[i]);
        }
    }

    /**
     * When removing a save event from unsaved events, we have to refresh the unsaved events which have been saved while saving this specific change
     * @param io
     */
    public void removeFromUnsaved(IntactObject io) {
        List<UnsavedChange> changes = getUnsavedChangesForCurrentUser();

        changes.remove(new UnsavedChange(io, UnsavedChange.CREATED, null));
        changes.remove(new UnsavedChange(io, UnsavedChange.UPDATED, null));

        removeObsoleteChangesOnSave(io);
    }

    public void removeFromCreatedTranscriptWithoutProtein(UnsavedChange unsavedChange) {
        getUnsavedChangesForCurrentUser().remove(unsavedChange);
    }

    public void removeFromDeleted(UnsavedChange unsavedChange) {
        getUnsavedChangesForCurrentUser().remove(unsavedChange);
    }

    public void removeFromDeleted(IntactObject object, AnnotatedObject parent) {
        String scope;

        if (parent != null && parent.getAc() != null){
            scope = parent.getAc();
        }
        else {
            scope = null;
        }

        getUnsavedChangesForCurrentUser().remove(new UnsavedChange(object, UnsavedChange.DELETED, parent, scope));
    }

    public void revert(AnnotatedObject io) {
        Iterator<UnsavedChange> iterator = getUnsavedChangesForCurrentUser().iterator();

        // removed the passed object from the list of unsaved changes. If this object is the scope of another change as well, delete it
        while (iterator.hasNext()) {
            UnsavedChange unsavedChange = iterator.next();

            // the object has an ac, we can compare using ac
            if (io.getAc() != null){
                if (io.getAc().equals(unsavedChange.getUnsavedObject().getAc()) || io.getAc().equals(unsavedChange.getScope())) {
                    iterator.remove();
                }
            }
            // the object is new, we can only checks if we have an unchanged event which is new and does not have a collection of parent acs (interactors, organism, cv terms)
            else if (unsavedChange.getUnsavedObject().getAc() == null) {

                if (unsavedChange.getUnsavedObject() instanceof AnnotatedObject){
                    AnnotatedObject unsavedAnnObj = (AnnotatedObject) unsavedChange.getUnsavedObject();

                    if (io.getShortLabel() != null && io.getShortLabel().equals(unsavedAnnObj.getShortLabel())){
                        iterator.remove();
                    }
                    else if (io.getShortLabel() == null && unsavedAnnObj.getShortLabel() == null && unsavedChange.getAcsToDeleteOn().isEmpty()){
                        iterator.remove();
                    }
                }
            }
        }
    }

    public void revertInteraction(Interaction interaction, Collection<String> parentAcs) {
        Iterator<UnsavedChange> iterator = getUnsavedChangesForCurrentUser().iterator();

        // removed the passed object from the list of unsaved changes. If this object is the scope of another change as well, delete it
        while (iterator.hasNext()) {
            UnsavedChange unsavedChange = iterator.next();

            // the object has an ac, we can compare using ac
            if (interaction.getAc() != null){
                if (interaction.getAc().equals(unsavedChange.getUnsavedObject().getAc()) || interaction.getAc().equals(unsavedChange.getScope())) {
                    iterator.remove();
                }
            }
            // the object is new, we can only checks if we have an unchanged event which is new and does not have a collection of parent acs (interactors, organism, cv terms)
            else if (unsavedChange.getUnsavedObject().getAc() == null) {
                if (unsavedChange.getUnsavedObject() instanceof Interaction){
                    Interaction unsavedInteraction = (Interaction) unsavedChange.getUnsavedObject();

                    if (interaction.getShortLabel() != null && interaction.getShortLabel().equals(unsavedInteraction.getShortLabel())){
                        iterator.remove();
                    }
                    // if both shortlabels are null, just checks parent acs
                    else if (interaction.getShortLabel() != null && unsavedInteraction.getShortLabel() == null) {
                        checkParentOfUnsavedObject(parentAcs, iterator, unsavedChange);
                    }
                }
            }
        }
    }

    public void revertExperiment(Experiment experiment, Collection<String> parentAcs) {
        Iterator<UnsavedChange> iterator = getUnsavedChangesForCurrentUser().iterator();

        // removed the passed object from the list of unsaved changes. If this object is the scope of another change as well, delete it
        while (iterator.hasNext()) {
            UnsavedChange unsavedChange = iterator.next();

            // the object has an ac, we can compare using ac
            if (experiment.getAc() != null){
                if (experiment.getAc().equals(unsavedChange.getUnsavedObject().getAc()) || experiment.getAc().equals(unsavedChange.getScope())) {
                    iterator.remove();
                }
            }
            // the object is new, we can only checks if we have an unchanged event which is new and does not have a collection of parent acs (interactors, organism, cv terms)
            else if (unsavedChange.getUnsavedObject().getAc() == null) {
                if (unsavedChange.getUnsavedObject() instanceof Experiment){
                    Experiment unsavedExperiment = (Experiment) unsavedChange.getUnsavedObject();

                    if (experiment.getShortLabel() != null && experiment.getShortLabel().equals(unsavedExperiment.getShortLabel())){
                        iterator.remove();
                    }
                    // if both shortlabels are null, just checks parent acs
                    else if (experiment.getShortLabel() == null && unsavedExperiment.getShortLabel() == null) {
                        checkParentOfUnsavedObject(parentAcs, iterator, unsavedChange);
                    }
                }
            }
        }
    }

    public void revertPublication(Publication publication) {
        Iterator<UnsavedChange> iterator = getUnsavedChangesForCurrentUser().iterator();

        // removed the passed object from the list of unsaved changes. If this object is the scope of another change as well, delete it
        while (iterator.hasNext()) {
            UnsavedChange unsavedChange = iterator.next();

            // the object has an ac, we can compare using ac
            if (publication.getAc() != null){
                if (publication.getAc().equals(unsavedChange.getUnsavedObject().getAc()) || publication.getAc().equals(unsavedChange.getScope())) {
                    iterator.remove();
                }
            }
            // the object is new, we can only checks if we have an unchanged event which is new and does not have a collection of parent acs (interactors, organism, cv terms)
            else if (unsavedChange.getUnsavedObject().getAc() == null) {
                if (unsavedChange.getUnsavedObject() instanceof Publication){
                    Publication unsavedPublication = (Publication) unsavedChange.getUnsavedObject();

                    if (publication.getShortLabel() != null && publication.getShortLabel().equals(unsavedPublication.getShortLabel())){
                        iterator.remove();
                    }
                    // if both shortlabels are null, just checks parent acs
                    else if (publication.getShortLabel() == null && unsavedPublication.getShortLabel() == null) {
                        iterator.remove();
                    }
                }
            }
        }
    }

    public void revertComponent(uk.ac.ebi.intact.model.Component component, Collection<String> parentAcs) {
        Iterator<UnsavedChange> iterator = getUnsavedChangesForCurrentUser().iterator();

        // removed the passed object from the list of unsaved changes. If this object is the scope of another change as well, delete it
        while (iterator.hasNext()) {
            UnsavedChange unsavedChange = iterator.next();
            // the object has an ac, we can compare using ac
            if (component.getAc() != null){
                if (component.getAc().equals(unsavedChange.getUnsavedObject().getAc()) || component.getAc().equals(unsavedChange.getScope())) {
                    iterator.remove();
                }
            }
            // the object is new, we can only checks if we have an unchanged event which is new and does not have a collection of parent acs (interactors, organism, cv terms)
            else if (unsavedChange.getUnsavedObject().getAc() == null) {
                if (unsavedChange.getUnsavedObject() instanceof uk.ac.ebi.intact.model.Component){
                    uk.ac.ebi.intact.model.Component unsavedComponent = (uk.ac.ebi.intact.model.Component) unsavedChange.getUnsavedObject();

                    if (component.getShortLabel() != null && component.getShortLabel().equals(unsavedComponent.getShortLabel())){
                        iterator.remove();
                    }
                    // if both shortlabels are null, just checks parent acs
                    else if (component.getShortLabel() == null && unsavedComponent.getShortLabel() == null) {
                        checkParentOfUnsavedObject(parentAcs, iterator, unsavedChange);
                    }
                }
            }
        }
    }

    public void revertFeature(Feature feature, Collection<String> parentAcs) {
        Iterator<UnsavedChange> iterator = getUnsavedChangesForCurrentUser().iterator();

        // removed the passed object from the list of unsaved changes. If this object is the scope of another change as well, delete it
        while (iterator.hasNext()) {
            UnsavedChange unsavedChange = iterator.next();
            // the object has an ac, we can compare using ac
            if (feature.getAc() != null){
                if (feature.getAc().equals(unsavedChange.getUnsavedObject().getAc()) || feature.getAc().equals(unsavedChange.getScope())) {
                    iterator.remove();
                }
            }
            // the object is new, we can only checks if we have an unchanged event which is new and does not have a collection of parent acs (interactors, organism, cv terms)
            else if (unsavedChange.getUnsavedObject().getAc() == null) {
                if (unsavedChange.getUnsavedObject() instanceof Feature){
                    Feature unsavedComponent = (Feature) unsavedChange.getUnsavedObject();

                    if (feature.getShortLabel() != null && feature.getShortLabel().equals(unsavedComponent.getShortLabel())){
                        iterator.remove();
                    }
                    // if both shortlabels are null, just checks parent acs
                    else if (feature.getShortLabel() == null && unsavedComponent.getShortLabel() == null) {
                        checkParentOfUnsavedObject(parentAcs, iterator, unsavedChange);
                    }
                }
            }
        }
    }

    private void checkParentOfUnsavedObject(Collection<String> parentAcs, Iterator<UnsavedChange> iterator, UnsavedChange unsavedChange) {
        // both parent acs are not saved, revert it
        if (parentAcs.isEmpty() && unsavedChange.getAcsToDeleteOn().isEmpty()){
            iterator.remove();
        }
        // if one of the parents is saved, checks that the parent acs of the unsaved changes are in common so we don't revert changes not saved which concerns another publication
        else if (!parentAcs.isEmpty()){
            boolean haveSameParents = true;
            for (String parentAc : parentAcs) {

                if (!unsavedChange.getAcsToDeleteOn().contains(parentAc)){
                    haveSameParents = false;
                }
            }

            if (haveSameParents){
                iterator.remove();
            }
        }
    }

    public boolean isUnsaved(IntactObject io) {
        if (io == null) return false;
        if (io.getAc() == null) return true;

        return isUnsavedAc(io.getAc());
    }

    public boolean isUnsavedOrDeleted(IntactObject io) {
        if (isUnsaved(io)) {
            return true;
        } else if (io.getAc() != null && isDeletedAc(io.getAc())) {
            return true;
        }

        return false;
    }

    public boolean isUnsavedAc(String ac) {
        if (ac == null) return true;

        for (UnsavedChange unsavedChange : getUnsavedChangesForCurrentUser()) {
            if (ac.equals(unsavedChange.getUnsavedObject().getAc())) {
                return true;
            }
        }

        return false;
    }

    public boolean isDeletedAc(String ac) {
        if (ac == null) return true;

        for (UnsavedChange unsavedChange : getUnsavedChangesForCurrentUser()) {
            if (UnsavedChange.DELETED.equals(unsavedChange.getAction()) &&
                    ac.equals(unsavedChange.getUnsavedObject().getAc())) {
                return true;
            }
        }

        return false;
    }

    public List<String> getDeletedAcs(Class type) {
        return DebugUtil.acList(getDeleted(type));
    }

    public List<String> getDeletedAcsByClassName(String className) {
        try {
            return getDeletedAcs(Thread.currentThread().getContextClassLoader().loadClass(className));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return Collections.EMPTY_LIST;
    }

    public List<IntactObject> getDeleted(Class type) {
        List<IntactObject> ios = new ArrayList<IntactObject>();

        for (UnsavedChange change : getUnsavedChangesForCurrentUser()) {
            if (UnsavedChange.DELETED.equals(change.getAction()) &&
                    type.isAssignableFrom(change.getUnsavedObject().getClass())) {
                IntactObject intactObject = change.getUnsavedObject();
                ios.add(intactObject);
            }
        }

        return ios;
    }

    public List<IntactObject> getAllUnsaved() {
        List<IntactObject> ios = new ArrayList<IntactObject>();

        for (UnsavedChange change : getUnsavedChangesForCurrentUser()) {
            if (UnsavedChange.UPDATED.equals(change.getAction()) || UnsavedChange.CREATED.equals(change.getAction())) {
                IntactObject intactObject = change.getUnsavedObject();
                ios.add(intactObject);
            }
        }

        return ios;
    }

    public List<IntactObject> getAllDeleted() {
        List<IntactObject> ios = new ArrayList<IntactObject>();

        for (UnsavedChange change : getUnsavedChangesForCurrentUser()) {
            if (UnsavedChange.DELETED.equals(change.getAction())) {
                IntactObject intactObject = change.getUnsavedObject();
                ios.add(intactObject);
            }
        }

        return ios;
    }

    public List<UnsavedChange> getAllUnsavedDeleted() {
        List<UnsavedChange> unsaved = new ArrayList<UnsavedChange>();

        for (UnsavedChange change : getUnsavedChangesForCurrentUser()) {
            if (UnsavedChange.DELETED.equals(change.getAction())) {
                unsaved.add(change);
            }
        }

        return unsaved;
    }

    public List<UnsavedChange> getAllUnsavedProteinTranscripts() {
        List<UnsavedChange> unsaved = new ArrayList<UnsavedChange>();

        for (UnsavedChange change : getUnsavedChangesForCurrentUser()) {
            if (UnsavedChange.CREATED_TRANSCRIPT.equals(change.getAction())) {
                unsaved.add(change);
            }
        }

        return unsaved;
    }

    public List<IntactObject> getAllCreatedProteinTranscripts() {
        List<IntactObject> ios = new ArrayList<IntactObject>();

        for (UnsavedChange change : getUnsavedChangesForCurrentUser()) {
            if (UnsavedChange.CREATED_TRANSCRIPT.equals(change.getAction())) {
                IntactObject intactObject = change.getUnsavedObject();
                ios.add(intactObject);
            }
        }

        return ios;
    }

    public IntactObject findByAc(String ac) {
        for (UnsavedChange change : getUnsavedChangesForCurrentUser()) {
            if (ac.equals(change.getUnsavedObject().getAc())) {
                return change.getUnsavedObject();
            }
        }

        return null;
    }

    public List<String> getUsernames() {
        return new ArrayList<String>(changesPerUser.keySet());
    }

    public boolean isObjectBeingEdited(IntactObject io, boolean includeMyself) {
        if (io.getAc() == null) return false;

        UserSessionController userSessionController = (UserSessionController) getSpringContext().getBean("userSessionController");
        String me = userSessionController.getCurrentUser().getLogin();

        for (String user : getUsernames()) {
            if (!includeMyself && user.equals(me)) continue;

            for (UnsavedChange unsavedChange : getUnsavedChangesForUser(user)) {
                if (io.getAc().equals(unsavedChange.getUnsavedObject().getAc())) {
                    return true;
                }
            }
        }

        return false;
    }

    //TODO: probably this should be a list
    public String whoIsEditingObject(IntactObject io) {
        if (io.getAc() == null) return null;

        for (String user : getUsernames()) {
            for (UnsavedChange unsavedChange : getUnsavedChangesForUser(user)) {
                if (io.getAc().equals(unsavedChange.getUnsavedObject().getAc())) {
                    return user;
                }
            }
        }

        return null;
    }

    public void clearCurrentUserChanges() {
        getUnsavedChangesForCurrentUser().clear();
    }

    private User getCurrentUser() {
        UserSessionController userSessionController = (UserSessionController) getSpringContext().getBean("userSessionController");
        return userSessionController.getCurrentUser();
    }

    public List<UnsavedChange> getUnsavedChangesForCurrentUser() {
        return getUnsavedChangesForUser(getCurrentUser().getLogin());
    }

    public List<UnsavedChange> getUnsavedChangesForUser(String userId) {
        List<UnsavedChange> unsavedChanges;

        if (changesPerUser.containsKey(userId)) {
            unsavedChanges = changesPerUser.get(userId);
        } else {
            unsavedChanges = new ArrayList<UnsavedChange>();
            changesPerUser.put(userId, unsavedChanges);
        }

        return unsavedChanges;
    }

    private void addChange(UnsavedChange unsavedChange) {
        List<UnsavedChange> unsavedChanges = getUnsavedChangesForCurrentUser();

        unsavedChanges.remove(unsavedChange);
        unsavedChanges.add(unsavedChange);
    }

    private boolean addUnsavedChange(UnsavedChange unsavedChange) {
        removeFromUnsaved(unsavedChange.getUnsavedObject());

        List<UnsavedChange> deletedChanges = getAllUnsavedDeleted();

        for (UnsavedChange deleteChange : deletedChanges){

            // if one deleted event is in conflict with the current save event, don't add an update event (if experiment is deleted, new changes on the interaction does not make any sense)
            if (unsavedChange.getAcsToDeleteOn().contains(deleteChange.getUnsavedObject().getAc())){
                return false;
            }
        }

        List<UnsavedChange> unsavedChanges = getUnsavedChangesForCurrentUser();
        unsavedChanges.add(unsavedChange);
        return true;
    }

    private void removeUserFromUnsaved(String user) {
        changesPerUser.remove(user);
    }
}
