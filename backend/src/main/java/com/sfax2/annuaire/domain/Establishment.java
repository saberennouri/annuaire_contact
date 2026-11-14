package com.sfax2.annuaire.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.BatchSize;

@Entity
@Table(name = "establishment", indexes = {
        @Index(name = "idx_establishment_category", columnList = "category"),
        @Index(name = "idx_establishment_delegation", columnList = "delegation")
})
public class Establishment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Kind kind;

    /** Délégation (معتمدية), en arabe comme dans le document source. */
    @Column(length = 60)
    private String delegation;

    /** Numéro d'ordre dans le document source. */
    private Integer number;

    @Column(nullable = false, length = 300)
    private String name;

    @Column(length = 200)
    private String email;

    @Column(length = 500)
    private String address;

    @Column(length = 300)
    private String note;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "establishment_phone", joinColumns = @JoinColumn(name = "establishment_id"))
    @OrderColumn(name = "position")
    @Column(name = "phone", length = 30)
    @BatchSize(size = 100)
    private List<String> phones = new ArrayList<>();

    @OneToMany(mappedBy = "establishment", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    @BatchSize(size = 100)
    private List<Contact> contacts = new ArrayList<>();

    /** Texte normalisé (voir ArabicNormalizer) utilisé par la recherche. */
    @Column(name = "search_text", length = 2000)
    private String searchText;

    public void addContact(Contact c) {
        c.setEstablishment(this);
        contacts.add(c);
    }

    public Long getId() { return id; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public Kind getKind() { return kind; }
    public void setKind(Kind kind) { this.kind = kind; }
    public String getDelegation() { return delegation; }
    public void setDelegation(String delegation) { this.delegation = delegation; }
    public Integer getNumber() { return number; }
    public void setNumber(Integer number) { this.number = number; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public List<String> getPhones() { return phones; }
    public void setPhones(List<String> phones) { this.phones = phones; }
    public List<Contact> getContacts() { return contacts; }
    public String getSearchText() { return searchText; }
    public void setSearchText(String searchText) { this.searchText = searchText; }
}
