/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.store.inmemory;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;

interface WriteCursorStrategy extends CursorStrategy {

    @Override
    WriteCursorStrategy enter(PathArgument arg);

    void delete(PathArgument arg);

    void merge(PathArgument arg, NormalizedNode<?, ?> data);

    void write(PathArgument arg, NormalizedNode<?, ?> data);

    void mergeToCurrent(NormalizedNodeContainer<?, ?, ?> data);

    void writeToCurrent(NormalizedNodeContainer<?, ?, ?> data);
}