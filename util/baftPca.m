data=load('baft76000.mat');
display(size(data.arr));
data=data.arr;
mean_data = mean(data');
%std_train = std(data.view1.training');
centered_data = bsxfun(@minus,data',mean_data); 
%norm_train = bsxfun(@rdivide,centered_train,std_train); 
c = cov(data');
[V,D] = eig(c);
%Sort V in D's order
V = fliplr(V);		% reverse order of eigenvectors
D = flipud(diag(D));	% extract eigenvalues anr reverse their orrer
[D,I]= sort((real(D)));	% sort reversed eigenvalues in ascending order
D = flipud(D);		% restore sorted eigenvalues into descending order
for j = 1:length(I)
  U(:,j) = V(:,I(j));  % sort reversed eigenvectors in ascending order
end
U = fliplr(U);	% restore sorted eigenvectors into descending order
data=U(1:40,:)*centered_data';
save('PCA40baft76000.mat','data');

%find number of PCs to take : 
%for i=1:969
%variance=variance+var(centered_data(:,i))
%end
%     total variance in data and then check the variance captured by 
%     first x components by doing sum(D(1:x))