%My log filter
function log = make2DLOG(sigma)
hg = zeros(3,3)
for i=1:3
    for j=1:3
        hg(i,j) = exp(-((i-2)^2+(j-2)^2)/(2*sigma^2));
    end
end
for i=1:3
    for j=1:3
        log(i,j) = ((i-2)^2+(j-2)^2-2*sigma^2)*hg(i,j)/(sigma^4*sum(sum(hg)));
    end
end
log = log-sum(sum(log))/9;
end