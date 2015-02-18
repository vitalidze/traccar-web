/*
 * Copyright 2015 Vitaly Litvak (vitavaque@gmail.com)
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
package org.traccar.web.shared.model;

import javax.persistence.*;

@Entity
@Table(name = "ui_state",
       uniqueConstraints = { @UniqueConstraint(name = "ui_state_user_name", columnNames = {"user_id", "name"}) })
public class UIStateEntry {
    public static final String ARCHIVE_GRID_STATE_ID = "archiveGrid";
    public static final String DEFAULT_ARCHIVE_GRID_STATE = "{\"hidden\":[\"valid\",\"address\"]}";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private long id;

    private String name;
    private String value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", foreignKey = @ForeignKey(name = "ui_state_fkey_user_id"))
    private User user;

    public UIStateEntry() {
    }

    public UIStateEntry(User user, String name, String value) {
        this.user = user;
        this.name = name;
        this.value = value;
    }

    public static UIStateEntry createDefaultArchiveGridStateEntry(User user) {
        return new UIStateEntry(user, ARCHIVE_GRID_STATE_ID, DEFAULT_ARCHIVE_GRID_STATE);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
