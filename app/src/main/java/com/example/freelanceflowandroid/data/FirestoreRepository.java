package com.example.freelanceflowandroid.data;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.freelanceflowandroid.data.model.Job;
import com.example.freelanceflowandroid.data.model.Message;
import com.example.freelanceflowandroid.data.model.Team;
import com.example.freelanceflowandroid.data.model.UserProfile;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class FirestoreRepository {
    private final FirebaseFirestore db;
    private ListenerRegistration jobsListener;
    private ListenerRegistration postsListener;
    private ListenerRegistration applicationsListener;

    public FirestoreRepository(Context context) {
        // reference the context so IDE doesn't warn it's unused
        Context appCtx = context == null ? null : context.getApplicationContext();

        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
        // appCtx can be used later if needed (keeps parameter referenced)
        if (appCtx != null) {
            // no-op, ensures the variable is used and avoids "never used" warnings
            appCtx.hashCode();
        }
    }

    public LiveData<Resource<Void>> saveUserProfile(UserProfile profile) {
        MutableLiveData<Resource<Void>> out = new MutableLiveData<>();
        out.postValue(Resource.loading());

        Map<String,Object> map = new HashMap<>();
        map.put("name", profile.name);
        map.put("email", profile.email);
        map.put("role", profile.role);
        map.put("teamId", profile.teamId);
        map.put("createdAt", FieldValue.serverTimestamp());

        db.collection("users").document(profile.uid)
                .set(map)
                .addOnSuccessListener(aVoid -> out.postValue(Resource.success(null)))
                .addOnFailureListener(e -> out.postValue(Resource.error(new Exception(e))));

        return out;
    }

    public void fetchUserRole(String uid, java.util.function.Consumer<String> onSuccess, java.util.function.Consumer<Exception> onError) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String role = doc.getString("role");
                        onSuccess.accept(role != null ? role : "FREELANCER");
                    } else {
                        onSuccess.accept("FREELANCER");
                    }
                })
                .addOnFailureListener(onError::accept);
    }

    public LiveData<Resource<String>> createJob(Job job) {
        MutableLiveData<Resource<String>> out = new MutableLiveData<>();
        out.postValue(Resource.loading());

        Map<String,Object> map = new HashMap<>();
        map.put("title", job.title);
        map.put("description", job.description);
        map.put("clientId", job.clientId);
        // your Job model provides a single 'budget' field (keeps compatibility)
        map.put("budget", job.budget);
        map.put("status", job.status != null ? job.status : "OPEN");
        map.put("createdAt", FieldValue.serverTimestamp());

        db.collection("jobs").add(map)
                .addOnSuccessListener(docRef -> out.postValue(Resource.success(docRef.getId())))
                .addOnFailureListener(e -> out.postValue(Resource.error(new Exception(e))));

        return out;
    }


    public void startJobsListener(String clientId, EventListener<QuerySnapshot> eventListener) {
        Query q = db.collection("jobs")
                .whereEqualTo("clientId", clientId)
                .orderBy("createdAt", Query.Direction.DESCENDING);
        jobsListener = q.addSnapshotListener(eventListener);
    }

    public void stopJobsListener() {
        if (jobsListener != null) {
            jobsListener.remove();
            jobsListener = null;
        }
    }

    /**
     * Start listening for public posts visible to all freelancers.
     * If you later add team-only visibility fields to Job, you can add additional listeners.
     */
    public void startPublicPostsListener(EventListener<QuerySnapshot> eventListener) {
        Query q = db.collection("jobs")
                // no visibility filter present in current model, fetch newest jobs
                .orderBy("createdAt", Query.Direction.DESCENDING);
        postsListener = q.addSnapshotListener(eventListener);
    }

    public void stopPublicPostsListener() {
        if (postsListener != null) {
            postsListener.remove();
            postsListener = null;
        }
    }

    /**
     * Create an application for a job (applicantId required).
     * applicantTeamId may be null (individual) or a teamId (applying as team).
     */
    public LiveData<Resource<String>> createApplication(String postId, String applicantId, @Nullable String applicantTeamId, @Nullable String coverLetter) {
        MutableLiveData<Resource<String>> out = new MutableLiveData<>();
        out.postValue(Resource.loading());

        Map<String, Object> map = new HashMap<>();
        map.put("postId", postId);
        map.put("applicantId", applicantId);
        if (applicantTeamId != null) map.put("applicantTeamId", applicantTeamId);
        map.put("coverLetter", coverLetter == null ? "" : coverLetter);
        map.put("status", "APPLIED");
        map.put("createdAt", FieldValue.serverTimestamp());

        db.collection("applications").add(map)
                .addOnSuccessListener(docRef -> out.postValue(Resource.success(docRef.getId())))
                .addOnFailureListener(e -> out.postValue(Resource.error(new Exception(e))));

        return out;
    }

    /**
     * Withdraw an application (mark status = WITHDRAWN)
     */
    public LiveData<Resource<Void>> withdrawApplication(String applicationId, String applicantId) {
        MutableLiveData<Resource<Void>> out = new MutableLiveData<>();
        out.postValue(Resource.loading());

        DocumentReference ref = db.collection("applications").document(applicationId);
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "WITHDRAWN");
        updates.put("updatedAt", FieldValue.serverTimestamp());

        ref.update(updates)
                .addOnSuccessListener(aVoid -> out.postValue(Resource.success(null)))
                .addOnFailureListener(e -> out.postValue(Resource.error(new Exception(e))));

        return out;
    }

    /**
     * Start listening for applications for a specific post (client views applicants).
     */
    public void startApplicationsListenerForPost(String postId, EventListener<QuerySnapshot> listener) {
        Query q = db.collection("applications")
                .whereEqualTo("postId", postId)
                .orderBy("createdAt", Query.Direction.DESCENDING);
        applicationsListener = q.addSnapshotListener(listener);
    }

    /**
     * Start listening for applications where applicantTeamId == teamId
     * (used by team admins to see applications submitted by their team).
     */
    public void startApplicationsListenerForTeam(String teamId, EventListener<QuerySnapshot> listener) {
        Query q = db.collection("applications")
                .whereEqualTo("applicantTeamId", teamId)
                .orderBy("createdAt", Query.Direction.DESCENDING);
        applicationsListener = q.addSnapshotListener(listener);
    }

    public void stopApplicationsListener() {
        if (applicationsListener != null) {
            applicationsListener.remove();
            applicationsListener = null;
        }
    }

    // Teams
    public LiveData<Resource<String>> createTeam(Team team, String adminUid) {
        MutableLiveData<Resource<String>> out = new MutableLiveData<>();
        out.postValue(Resource.loading());

        Map<String,Object> map = new HashMap<>();
        map.put("name", team.name);
        map.put("adminId", adminUid);
        map.put("memberIds", java.util.Arrays.asList(adminUid));
        map.put("createdAt", FieldValue.serverTimestamp());

        db.collection("teams").add(map)
                .addOnSuccessListener(docRef -> {
                    // Also update user's teamId
                    db.collection("users").document(adminUid).update("teamId", docRef.getId())
                            .addOnSuccessListener(aVoid -> out.postValue(Resource.success(docRef.getId())))
                            .addOnFailureListener(e -> out.postValue(Resource.error(new Exception(e))));
                })
                .addOnFailureListener(e -> out.postValue(Resource.error(new Exception(e))));

        return out;
    }

    public LiveData<Resource<Boolean>> joinTeam(String teamId, String userId) {
        MutableLiveData<Resource<Boolean>> out = new MutableLiveData<>();
        out.postValue(Resource.loading());

        DocumentReference teamRef = db.collection("teams").document(teamId);
        DocumentReference userRef = db.collection("users").document(userId);

        db.runTransaction(transaction -> {
                    DocumentSnapshot teamSnap = transaction.get(teamRef);
                    if (!teamSnap.exists()) throw new RuntimeException("Team does not exist");
                    java.util.List<String> members = (java.util.List<String>) teamSnap.get("memberIds");
                    if (members == null) members = new java.util.ArrayList<>();
                    if (!members.contains(userId)) members.add(userId);
                    transaction.update(teamRef, "memberIds", members);
                    transaction.update(userRef, "teamId", teamId);
                    return true;
                }).addOnSuccessListener(aBoolean -> out.postValue(Resource.success(true)))
                .addOnFailureListener(e -> out.postValue(Resource.error(new Exception(e))));

        return out;
    }

    // Messages (team chat)
    public LiveData<Resource<Void>> sendMessage(String teamId, Message message) {
        MutableLiveData<Resource<Void>> out = new MutableLiveData<>();
        out.postValue(Resource.loading());

        Map<String,Object> map = new HashMap<>();
        map.put("senderId", message.senderId);
        map.put("text", message.text);
        map.put("createdAt", FieldValue.serverTimestamp());

        db.collection("teams").document(teamId).collection("messages")
                .add(map)
                .addOnSuccessListener(docRef -> out.postValue(Resource.success(null)))
                .addOnFailureListener(e -> out.postValue(Resource.error(new Exception(e))));

        return out;
    }

    // Utility getters
    public FirebaseFirestore getFirestore() {
        return db;
    }
}