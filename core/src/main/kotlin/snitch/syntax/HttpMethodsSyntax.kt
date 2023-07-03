package snitch.syntax

import snitch.syntax.methods.*

interface HttpMethodsSyntax :
    GetMethodSyntax,
    PostMethodSyntax,
    PutMethodSyntax,
    DeleteMethodSyntax,
    PatchMethodSyntax,
    OptionsMethodSyntax,
    HeadMethodSyntax