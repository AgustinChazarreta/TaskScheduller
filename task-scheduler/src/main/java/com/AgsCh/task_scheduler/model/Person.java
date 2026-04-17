package com.AgsCh.task_scheduler.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;

@Entity
@Table(name = "persons")
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Identificador único

    @Column(name = "full_name", nullable = false)
    private String fullName; // Nombre completo

    @Column(name = "nick_name")
    private String nickName; // Nombre de guerra

    @Column(name = "birth_date")
    private LocalDate birthDate; // Fecha de nacimiento


    private String email;

    @Column(name = "email_notifications_enabled")
    private boolean emailNotificationsEnabled = false;

    @Column
    private boolean active = true;

    @Column(name = "entry_date")
    private LocalDate entryDate;

    @Column(name = "exit_date")
    private LocalDate exitDate;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "profile_image_public_id")
    private String profileImagePublicId;

    // Patrón semanal (normalmente)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "person_working_days", joinColumns = @JoinColumn(name = "person_id"))
    @Column(name = "day_of_week")
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeek> workingDays = EnumSet.noneOf(DayOfWeek.class);

    // ---- Relaciones ----
    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PersonUnavailability> unavailabilities = new ArrayList<>();

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PersonFunction> personFunctions = new ArrayList<>();

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FunctionAssignment> functionAssignments = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "house_id", nullable = false)
    private House house;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @OneToOne(mappedBy = "person")
    private User user;

    // --------- Constructores ---------
    public Person() {
    }

    public Person(String fullName, String nickName, LocalDate birthDate, String email,
            boolean emailNotificationsEnabled, boolean active,
            LocalDate entryDate, LocalDate exitDate, Set<DayOfWeek> workingDays) {
        this.fullName = fullName;
        this.nickName = nickName;
        this.birthDate = birthDate;
        this.email = email;
        this.emailNotificationsEnabled = emailNotificationsEnabled;
        this.active = active;
        this.entryDate = entryDate;
        this.exitDate = exitDate;
        this.workingDays = workingDays != null ? EnumSet.copyOf(workingDays) : EnumSet.noneOf(DayOfWeek.class);
    }

    // ---- Getters ----

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getNickName() {
        return nickName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getEmail() {
        return email;
    }

    public boolean isEmailNotificationsEnabled() {
        return emailNotificationsEnabled;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public LocalDate getExitDate() {
        return exitDate;
    }

    public Set<DayOfWeek> getWorkingDays() {
        return workingDays;
    }

    public List<PersonUnavailability> getUnavailabilities() {
        return unavailabilities;
    }

    public List<PersonFunction> getPersonFunctions() {
        return personFunctions;
    }

    public List<FunctionAssignment> getFunctionAssignments() {
        return functionAssignments;
    }

    // ---- Setters ----

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public void setEmail(String email) {

        // evitar trabajo innecesario
        if (email != null && email.equals(this.email)) {
            return;
        }

        this.email = email;

        // sincronizar con el User si existe
        if (this.user != null && email != null && !email.equals(this.user.getUsername())) {
            this.user.setUsername(email);
        }
    }

    public void setEmailNotificationsEnabled(boolean enabled) {
        this.emailNotificationsEnabled = enabled;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }

    public void setExitDate(LocalDate exitDate) {
        this.exitDate = exitDate;
    }

    public void setWorkingDays(Set<DayOfWeek> workingDays) {
        this.workingDays = workingDays != null
                ? EnumSet.copyOf(workingDays)
                : EnumSet.noneOf(DayOfWeek.class);
    }

    // ---- Helpers de dominio (opcional pero elegante) ----

    public boolean worksOn(DayOfWeek dayOfWeek) {
        return workingDays.contains(dayOfWeek);
    }

    public void addUnavailability(PersonUnavailability u) {
        unavailabilities.add(u);
        u.setPerson(this);
    }

    public void removeUnavailability(PersonUnavailability u) {
        unavailabilities.remove(u);
        u.setPerson(null);
    }

    public void addPersonFunction(PersonFunction pf) {
        personFunctions.add(pf);
        pf.setPerson(this);
    }

    public void removePersonFunction(PersonFunction pf) {
        personFunctions.remove(pf);
        pf.setPerson(null);
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImagePublicId(String profileImagePublicId) {
        this.profileImagePublicId = profileImagePublicId;
    }

    public String getProfileImagePublicId() {
        return profileImagePublicId;
    }

    public void setHouse(House house) {
        this.house = house;
    }

    public House getHouse() {
        return house;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public void setUser(User user) {
        this.user = user;

        if (user != null && user.getPerson() != this) {
            user.setPerson(this);
        }

        // sincronizar email/username
        if (user != null && this.email != null) {
            user.setUsername(this.email);
        }
    }

    public User getUser() {
        return user;
    }

    @Override
    public String toString() {
        return nickName;
    }
}