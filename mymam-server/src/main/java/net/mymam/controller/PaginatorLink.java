/* MyMAM - Open Source Digital Media Asset Management.
 * http://www.mymam.net
 *
 * Copyright 2013, MyMAM contributors as indicated by the @author tag.
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
package net.mymam.controller;


/**
 * @author fstab
 */
public class PaginatorLink {

    public enum State {
        DISABLED("disabled"),
        ACTIVE("active"),
        NONE("");

        private final String css;

        private State(String css) {
            this.css = css;
        }

        @Override
        public String toString() {
            return css;
        }
    }

    private final String label;
    private final int targetPage;
    private final State state;

    public PaginatorLink(int targetPage) {
        this("" + targetPage, targetPage);
    }

    public PaginatorLink(String label, int targetPage) {
        this(label, targetPage, State.NONE);
    }

    public PaginatorLink(int targetPage, State state) {
        this("" + targetPage, targetPage, state);
    }

    public PaginatorLink(String label, int targetPage, State state) {
        this.label = label;
        this.targetPage = targetPage;
        this.state = state;
    }

    public String getLabel() {
        return label;
    }

    public int getTargetPage() {
        return targetPage;
    }

    public String getStyleClass() {
        return state.toString();
    }
}
